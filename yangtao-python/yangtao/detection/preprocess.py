import random
from pathlib import Path

import cv2
import numpy as np
from matplotlib import pyplot as plt

def remove_shadow(img):
    rgb_planes = cv2.split(img)

    result_planes = []
    result_norm_planes = []
    for plane in rgb_planes:
        dilated_img = cv2.dilate(plane, np.ones((7, 7), np.uint8))
        bg_img = cv2.medianBlur(dilated_img, 21)
        diff_img = 255 - cv2.absdiff(plane, bg_img)
        norm_img = cv2.normalize(diff_img, None, alpha=0, beta=255, norm_type=cv2.NORM_MINMAX, dtype=cv2.CV_8UC1)
        result_planes.append(diff_img)
        result_norm_planes.append(norm_img)

    result = cv2.merge(result_planes)
    result_norm = cv2.merge(result_norm_planes)

    return result_norm

def detect_character(path: Path):
    img = cv2.imread(str(path))
    original = img.copy()

    img = remove_shadow(img)

    img = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
    img = cv2.threshold(img, 0, 255, cv2.THRESH_BINARY | cv2.THRESH_OTSU)[1]
    contours, hierarchy = cv2.findContours(img, 1, 2)

    for contour in sorted(contours, key=cv2.contourArea, reverse=True)[:2]:
        M = cv2.moments(contour)
        cx = int(M['m10']/M['m00'])
        cy = int(M['m01']/M['m00'])

        area = cv2.contourArea(contour)
        print(area)

        x,y,w,h = cv2.boundingRect(contour)
        cv2.rectangle(original,(x,y),(x+w,y+h),(0,255,0),2)

    img = cv2.resize(original, (0,0), fx=0.25, fy=0.25)

    cv2.imshow("gray" + str(random.randint(0, 10000)), img)


if __name__ == '__main__':
    home = Path(r"E:\git\yangtao\yangtao-python\data\raw\children_book")
    for p in home.iterdir():
        detect_character(p)


    cv2.waitKey(0)
    cv2.destroyAllWindows()