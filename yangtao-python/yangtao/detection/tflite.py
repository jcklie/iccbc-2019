import tensorflow as tf

from yangtao.config import PATH_DATA_RESULTS

model = tf.keras.models.load_model(PATH_DATA_RESULTS / "model.h5")
converter = tf.lite.TFLiteConverter.from_keras_model(model)
tflite_model = converter.convert()

with open(PATH_DATA_RESULTS / 'model.tflite', 'wb') as f:
  f.write(tflite_model)