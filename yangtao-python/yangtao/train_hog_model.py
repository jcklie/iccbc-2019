import logging
import pickle
from typing import Tuple, List, Dict

import warnings

from yangtao.util import setup_logging

warnings.simplefilter(action='ignore', category=FutureWarning)

import cv2

from PIL import Image

import tensorflow as tf
from tensorflow import keras
from tensorflow.python.keras.callbacks import EarlyStopping

import numpy as np

from tqdm import tqdm

import attr

from yangtao.config import *

EPOCHS = 100

@attr.s
class DataFrame:
    X_train: np.array = attr.ib()
    y_train: np.array = attr.ib()
    paths_train: List[Path] = attr.ib()
    X_test: np.array = attr.ib()
    y_test: np.array = attr.ib()
    path_test: List[Path] = attr.ib()
    input_size: int = attr.ib()
    num_classes: int = attr.ib()
    labels_to_idx: Dict[str, int] = attr.ib()
    idx_to_label: List[str] = attr.ib()


def compute_hog(path_train: Path, path_test: Path, dest: Path):
    logging.info(f"Computing HOG features for [{path_train}] and [{path_test}], saving to [{dest}]")
    winSize = (64, 64)
    blockSize = (16, 16)
    blockStride = (8, 8)
    cellSize = (8, 8)
    nbins = 9

    hog = cv2.HOGDescriptor(winSize, blockSize, blockStride, cellSize, nbins)

    def convert_single(p: Path) -> Tuple[np.array, np.array, List[Path], Dict[str, int], List[str]]:
        idx_to_label = [x.name for x in sorted(p.iterdir()) ]
        labels_to_idx = {x: i for i, x in enumerate(idx_to_label)}

        X = []
        y = []
        paths = []

        kernel = cv2.getStructuringElement(shape=cv2.MORPH_RECT, ksize=(5, 5), anchor=(2, 2))

        for folder in tqdm(list(sorted(p.iterdir()))):
            label = folder.name

            for image_path in folder.iterdir():
                assert image_path.exists()

                # opencv can sometimes not read file paths with unicode, so
                # we use this hack
                with open(image_path, "rb") as f:
                    bytes = bytearray(f.read())
                    arr = np.asarray(bytes, dtype=np.uint8)
                    image = cv2.imdecode(arr, cv2.IMREAD_UNCHANGED)

                # cv2.morphologyEx(image, cv2.MORPH_OPEN, kernel)
                # cv2.morphologyEx(image, cv2.MORPH_CLOSE, kernel)

                # hogs = hog(image, pixels_per_cell=(6, 6), cells_per_block=(1, 1))

                hogs = hog.compute(image).squeeze()
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
        hog.getDescriptorSize(), len(labels_to_idx_train), labels_to_idx_train, idx_to_label_train
    )

    with open(dest, "wb") as f:
        pickle.dump(df, f)


def load_hog(path: Path) -> DataFrame:
    with open(path, "rb") as f:
        return pickle.load(f)


def train_hog(path: Path):
    df = load_hog(path)

    y_train = tf.one_hot(df.y_train, df.num_classes)
    y_test = tf.one_hot(df.y_test, df.num_classes)

    model = keras.Sequential([
        keras.layers.Dense(512, input_shape=(df.input_size,), activation=tf.nn.relu),
        keras.layers.Dropout(0.5),
        keras.layers.Dense(256, activation=tf.nn.relu),
        keras.layers.Dropout(0.5),
        keras.layers.Dense(df.num_classes, activation=tf.nn.softmax)
    ])

    earlystop_callback = EarlyStopping(
        monitor='accuracy', min_delta=0.0001,
        patience=5)

    model.compile(optimizer='adam',
                  loss='categorical_crossentropy',
                  metrics=['accuracy', tf.keras.metrics.TopKCategoricalAccuracy(10)])

    history = model.fit(x=df.X_train, y=y_train,
                        epochs=EPOCHS,
                        verbose=2,
                        validation_data=(df.X_test, y_test),
                        callbacks=[earlystop_callback])

    model.save(PATH_DATARESULT_SPCCI_HOG)
    with open(PATH_DATARESULT_SPCCI_HOG_LABELS, "w", encoding="utf-8") as f:
        for label in df.idx_to_label:
            f.write(label)
            f.write("\n")


def main():
    setup_logging()
    # compute_hog(PATH_DATA_GENERATED_SPCCI_120_TRAIN, PATH_DATA_GENERATED_SPCCI_120_TEST, PATH_DATA_GENERATED_SPCCI_120_HOG)
    compute_hog(PATH_DATA_GENERATED_SPCCI_280_TRAIN, PATH_DATA_GENERATED_SPCCI_280_TEST, PATH_DATA_GENERATED_SPCCI_280_HOG)

    # train_hog(PATH_DATA_GENERATED_SPCCI_120_HOG)
    train_hog(PATH_DATA_GENERATED_SPCCI_280_HOG)


if __name__ == '__main__':
    main()