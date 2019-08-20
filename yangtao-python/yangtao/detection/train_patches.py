import tensorflow as tf

import matplotlib.pyplot as plt
from tensorflow.python.keras import optimizers
from tensorflow.python.keras.callbacks import EarlyStopping

from yangtao.config import *

PATH_DATA_RESULTS.mkdir(parents=True, exist_ok=True)

DATA_TRAIN_DIR = PATH_DATA_GENERATED_SPCCI_120_TRAIN
DATA_VALID_DIR = PATH_DATA_GENERATED_SPCCI_120_TEST

print(f"Train: {DATA_TRAIN_DIR}")
print(f"Valid: {DATA_VALID_DIR}")

IMAGE_SIZE = 96
BATCH_SIZE = 64
IMG_SHAPE = (IMAGE_SIZE, IMAGE_SIZE, 3)
EPOCHS = 2

import warnings
warnings.simplefilter(action='ignore', category=FutureWarning)

def preprocess(X):
    return X

datagen = tf.keras.preprocessing.image.ImageDataGenerator(
    preprocessing_function=tf.keras.applications.mobilenet_v2.preprocess_input,
    # rotation_range=5,
    # shear_range=0.01,
)

train_generator = datagen.flow_from_directory(
    DATA_TRAIN_DIR,
    target_size=(IMAGE_SIZE, IMAGE_SIZE),
    batch_size=BATCH_SIZE,
    #color_mode="grayscale")
)

val_generator = datagen.flow_from_directory(
    DATA_VALID_DIR,
    target_size=(IMAGE_SIZE, IMAGE_SIZE),
    batch_size=BATCH_SIZE,
    #color_mode="grayscale")
)

labels = '\n'.join(sorted(train_generator.class_indices.keys()))
number_of_classes = len(train_generator.class_indices)
print(f"Number of classes: {number_of_classes}")

with open(PATH_DATA_RESULTS / 'labels.txt', 'w') as f:
  f.write(labels)


base_model = tf.keras.applications.MobileNetV2(input_shape=IMG_SHAPE, include_top=True, classes=number_of_classes, weights=None)

# base_model.trainable = False

"""
model = tf.keras.Sequential([
  base_model,
  tf.keras.layers.Conv2D(32, 3, activation='relu'),
  tf.keras.layers.Dropout(0.5),
  tf.keras.layers.GlobalAveragePooling2D(),
  tf.keras.layers.Dense(number_of_classes, activation='softmax')
])
"""

model = base_model
model.compile(optimizer='rmsprop',
              loss='categorical_crossentropy',
              metrics=['accuracy'])

earlystop_callback = EarlyStopping(
  monitor='val_accuracy', min_delta=0.0001,
  patience=5)

history = model.fit(train_generator,
                    epochs=EPOCHS,
                    verbose=1,
                    validation_data=val_generator,
                    callbacks=[earlystop_callback])

model.save(PATH_DATA_RESULTS / "model.h5")

acc = history.history['accuracy']
val_acc = history.history['val_accuracy']

loss = history.history['loss']
val_loss = history.history['val_loss']

plt.figure(figsize=(8, 8))
plt.subplot(2, 1, 1)
plt.plot(acc, label='Training Accuracy')
plt.plot(val_acc, label='Validation Accuracy')
plt.legend(loc='lower right')
plt.ylabel('Accuracy')
plt.ylim([min(plt.ylim()),1])
plt.title('Training and Validation Accuracy')

plt.subplot(2, 1, 2)
plt.plot(loss, label='Training Loss')
plt.plot(val_loss, label='Validation Loss')
plt.legend(loc='upper right')
plt.ylabel('Cross Entropy')
plt.ylim([0,1.0])
plt.title('Training and Validation Loss')
plt.xlabel('epoch')
plt.show()