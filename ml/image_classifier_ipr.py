import sys

import tensorflow as tf


def main():
    m_p, l_p, i_p = sys.argv[1:4]
    interpretor = tf.lite.Interpreter(model_path=m_p)
    interpretor.allocate_tensors()
    i_details = interpretor.get_input_details()
    o_details = interpretor.get_output_details()
    i_shape = i_details[0]["shape"][1:3]
    i_dtype = i_details[0]["dtype"]
    img = tf.keras.preprocessing.image.load_img(i_p, target_size=i_shape)
    x = tf.keras.preprocessing.image.img_to_array(img, dtype=i_dtype)
    interpretor.set_tensor(i_details[0]["index"], (x, ))
    interpretor.invoke()
    y = interpretor.get_tensor(o_details[0]["index"])
    with open(l_p) as f:
        labels = f.readlines()
    f_o = {}
    for i in range(len(y[0])):
        f_o[labels[i].strip()] = y[0][i]
    f_o = sorted(f_o.items(), key=lambda x: x[1], reverse=True)
    if len(sys.argv) > 4:
        f_ns = int(sys.argv[4])
    for i in f_o[:(f_ns + 1)]:
        print(f"{i[0]}: {i[1]}")


if __name__ == "__main__":
    main()
