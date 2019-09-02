import pickle
from dataclasses import dataclass
from pathlib import Path
from typing import Tuple, List, Dict

import warnings
warnings.simplefilter(action='ignore', category=FutureWarning)

import matplotlib
import matplotlib.pyplot as plt

from PIL import Image

from skimage.feature import hog
from skimage import data, exposure

import tensorflow as tf
from tensorflow import keras
from tensorflow.python.keras.callbacks import EarlyStopping

import numpy as np

from tqdm import tqdm

from yangtao.config import *

EPOCHS = 100

@dataclass
class DataFrame:
    X_train: np.array
    y_train: np.array
    paths_train: List[Path]
    X_test: np.array
    y_test: np.array
    path_test: List[Path]
    num_classes: int
    labels_to_idx: Dict[str, int]
    idx_to_label: Dict[int, str]


def compute_hog(path_train: Path, path_test: Path, dest: Path):
    def convert_single(p: Path) -> Tuple[np.array, np.array, List[Path], Dict[int, str], Dict[str, int]]:
        idx_to_label = {i: x.name for i, x in enumerate(sorted(p.iterdir())) }
        labels_to_idx = {x.name: i for i, x in enumerate(sorted(p.iterdir()))}

        X = []
        y = []
        paths = []

        for folder in tqdm(list(sorted(p.iterdir()))):
            label = folder.name

            for image_path in folder.iterdir():
                image = Image.open(image_path)
                hogs = hog(image, pixels_per_cell=(6, 6), cells_per_block=(1, 1))

                X.append(hogs)
                y.append(labels_to_idx[label])
                paths.append(image_path)

        return np.array(X), np.array(y), paths, labels_to_idx, idx_to_label

    X_train, y_train, paths_train, labels_to_idx_train, idx_to_label_train = convert_single(path_train)
    X_test, y_test, paths_test, labels_to_idx_test, idx_to_label_test = convert_single(path_test)

    assert labels_to_idx_train == labels_to_idx_test
    assert idx_to_label_train == idx_to_label_test

    df = DataFrame(
        X_train, y_train, path_train,
        X_test,  y_test,  paths_test,
        len(labels_to_idx_train), labels_to_idx_train, idx_to_label_train
    )

    with open(dest, "wb") as f:
        pickle.dump(df, f)

def debug_image(image, fd):
    fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(8, 4), sharex=True, sharey=True)

    ax1.axis('off')
    ax1.imshow(image, cmap=plt.cm.gray)
    ax1.set_title('Input image')

    # Rescale histogram for better display
    hog_image_rescaled = exposure.rescale_intensity(hog_image, in_range=(0, 10))

    ax2.axis('off')
    ax2.imshow(hog_image_rescaled, cmap=plt.cm.gray)
    ax2.set_title('Histogram of Oriented Gradients')
    plt.show()


def load_hog(path: Path) -> DataFrame:
    with open(path, "rb") as f:
        return pickle.load(f)

def train_hog(path: Path):
    df = load_hog(path)

    y_train = tf.one_hot(df.y_train, df.num_classes)
    y_test = tf.one_hot(df.y_test, df.num_classes)

    model = keras.Sequential([
        keras.layers.Dense(256, input_shape=(900,), activation=tf.nn.relu),
        keras.layers.Dropout(0.5),
        keras.layers.Dense(128, activation=tf.nn.relu),
        keras.layers.Dropout(0.5),
        keras.layers.Dense(df.num_classes, activation=tf.nn.softmax)
    ])

    earlystop_callback = EarlyStopping(
        monitor='accuracy', min_delta=0.0001,
        patience=5)

    model.compile(optimizer='adam',
                  loss='categorical_crossentropy',
                  metrics=['accuracy'])

    history = model.fit(x=df.X_train, y=y_train,
                        epochs=EPOCHS,
                        verbose=1,
                        validation_data=(df.X_test, y_test),
                        callbacks=[earlystop_callback])

    model.save(PATH_DATARESULT_SPCCI_HOG)


def visualize_predictions(path: Path):
    model =  keras.models.load_model(PATH_DATARESULT_SPCCI_HOG)
    df = load_hog(path)
    y_test = tf.one_hot(df.y_test, len(df.labels_to_idx))

    predicted_id = model.predict_classes(df.X_test)

    font_name = "Noto Serif CJK SC"
    matplotlib.rcParams['font.family'] = font_name
    matplotlib.rcParams['axes.unicode_minus'] = False

    k = 16 * 9
    idx = np.random.choice(len(df.y_test), k)

    plt.figure(figsize=(10, 9))
    plt.subplots_adjust(hspace=0.5)
    for n in range(k):
        plt.subplot(16, 9, n + 1)
        n = idx[n]
        img = Image.open(df.path_test[n])
        plt.imshow(img, cmap="gray")
        color = "green" if predicted_id[n] == df.y_test[n] else "red"
        plt.title(df.idx_to_label[predicted_id[n]], color=color)
        plt.axis('off')
    _ = plt.suptitle("Model predictions (green: correct, red: incorrect)")

    plt.show()

def main():
    # compute_hog(PATH_DATA_GENERATED_SPCCI_120_TRAIN, PATH_DATA_GENERATED_SPCCI_120_TEST, PATH_DATA_GENERATED_SPCCI_120_HOG)
    # compute_hog(PATH_DATA_GENERATED_SPCCI_280_TRAIN, PATH_DATA_GENERATED_SPCCI_280_TEST, PATH_DATA_GENERATED_SPCCI_280_HOG)

    train_hog(PATH_DATA_GENERATED_SPCCI_280_HOG)

    # visualize_predictions(PATH_DATA_GENERATED_SPCCI_280_HOG)


if __name__ == '__main__':
    main()