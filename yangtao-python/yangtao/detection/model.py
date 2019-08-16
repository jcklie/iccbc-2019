from dataclasses import dataclass
from pathlib import Path
from typing import List, Tuple

import tensorflow as tf

from sklearn.model_selection import train_test_split

from yangtao.config import PATH_DATA_GENERATED_HANZI

IMAGE_SIZE = 96 # Minimum image size for use with MobileNetV2
BATCH_SIZE = 32
IMG_SHAPE = (IMAGE_SIZE, IMAGE_SIZE, 1)
AUTOTUNE = tf.data.experimental.AUTOTUNE

@dataclass
class DataSet:
    ds: tf.data.Dataset
    size: int

def build_model(number_of_classes: int) -> tf.keras.Model:
    base_model = tf.keras.applications.MobileNetV2(
        input_shape=IMG_SHAPE,
        include_top=True,
        classes=number_of_classes,
        weights=None,
    )

    return base_model

def load_data() -> Tuple[DataSet, DataSet, int]:
    all_image_paths = list(PATH_DATA_GENERATED_HANZI.iterdir())
    all_labels = [p.name[0] for p in all_image_paths]
    label_names = set(all_labels)
    label_to_index = dict((name, index) for index, name in enumerate(label_names))
    number_of_classes = len(label_names)

    def build_single(image_paths: List[Path], name: str) -> DataSet:
        ds_size = len(image_paths)
        image_ds = tf.data.Dataset.from_tensor_slices([str(x) for x in image_paths]).map(tf.io.read_file)
        tfrec = tf.data.experimental.TFRecordWriter(name + '.tfrec')
        tfrec.write(image_ds)
        image_ds = tf.data.TFRecordDataset(name +'.tfrec').map(preprocess_image)

        # Labels
        image_labels = [label_to_index[p.name[0]] for p in image_paths]
        label_ds = tf.data.Dataset.from_tensor_slices(tf.cast(image_labels, tf.int64))

        # Combining
        ds = tf.data.Dataset.zip((image_ds, label_ds))
        ds = ds.shuffle(buffer_size=ds_size)
        ds = ds.repeat()
        ds = ds.batch(BATCH_SIZE)

        return DataSet(ds, ds_size)

    train_paths, test_paths = train_test_split(all_image_paths, test_size=.1, stratify=all_labels)
    ds_train = build_single(train_paths, name = "train")
    ds_test = build_single(test_paths, name = "test")

    return ds_train, ds_test, number_of_classes


def preprocess_image(image):
    image = tf.image.decode_png(image, channels=1)
    image = tf.image.resize(image, [IMAGE_SIZE, IMAGE_SIZE])

    # normalize to [-1,1] range
    return tf.keras.applications.mobilenet_v2.preprocess_input(image)

if __name__ == '__main__':
    ds_train, ds_dev, number_of_classes = load_data()
    train_steps_per_epoch = tf.math.ceil(ds_train.size / BATCH_SIZE).numpy()
    val_steps_per_epoch = tf.math.ceil(ds_dev.size / BATCH_SIZE).numpy()

    model = build_model(number_of_classes)
    model.compile(optimizer=tf.keras.optimizers.Adam(),
                  loss='sparse_categorical_crossentropy',
                  metrics=["accuracy"])

    print(model.summary())
    print(ds_train)
    print(ds_dev)

    callback = tf.keras.callbacks.EarlyStopping(monitor='val_loss', patience=3)

    model.fit(ds_train.ds, validation_data=ds_train.ds, epochs=100, steps_per_epoch=train_steps_per_epoch,
              validation_steps=val_steps_per_epoch, callbacks=[callback])