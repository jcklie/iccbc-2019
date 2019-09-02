import tensorflow as tf

from yangtao.config import PATH_DATARESULT_SPCCI_HOG

converter = tf.lite.TFLiteConverter.from_keras_model_file(PATH_DATARESULT_SPCCI_HOG)
converter
tflite_model = converter.convert()
open("model.tflite", "wb").write(tflite_model)