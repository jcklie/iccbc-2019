import random
import shutil

from pathlib import Path
from typing import List

import numpy as np
from tqdm import tqdm

from fontTools.ttLib import TTFont
from fontTools.unicode import Unicode

from PIL import Image, ImageDraw, ImageFont
from PIL.ImageFont import FreeTypeFont

from yangtao.config import PATH_DATA_FONTS, PATH_DATA_GENERATED_HANZI, PATH_DATA_BACKGROUNDS
from yangtao.hanzi import get_most_frequent_characters

SIZE = 96


def generate_random_background(bg: Image) -> Image:
    w, h = bg.size
    x = random.randint(0, w - SIZE)
    y = random.randint(0, h - SIZE)

    patch = bg.crop((x, y, x + SIZE, y + SIZE))

    return patch

def draw_hanzi(hanzi: str, font: FreeTypeFont, target_name: str):
    # https://stackoverflow.com/questions/4458696/finding-out-what-characters-a-given-font-supports
    # assert bg.size == (SIZE,SIZE), f"Expected image size to be [{SIZE}], but was [{bg.size}]"

    image = np.zeros(shape=(SIZE,SIZE),dtype=np.uint8)
    im = Image.fromarray(image)

    width, height = font.getsize(hanzi)

    draw = ImageDraw.Draw(im)
    draw.text(((SIZE - width) / 2, (SIZE - height) / 2), hanzi, (255,),font=font)
    # p  = cv2.cvtColor(p,cv2.COLOR_RGB2BGR)

    im.save(target_name, "png")


def main():
    BGS = list(PATH_DATA_BACKGROUNDS.iterdir())
    characters = get_most_frequent_characters(100)
    fonts = list(PATH_DATA_FONTS.iterdir())

    for i, font_path in enumerate(tqdm(fonts)):
        try:
            font = ImageFont.truetype(str(font_path), 80)
            ttf = TTFont(font_path, 0, verbose=0, allowVID=0, ignoreDecompileErrors=True)
        except Exception as e:
            print(e, font_path)
            continue

        # print(i, font_path)

        supported_chars = {e[0] for table in ttf["cmap"].tables for e in table.cmap.items() }

        j = 0

        bg_path = random.choice(BGS)
        bg = Image.open(bg_path)

        for c in characters:
            if ord(c) not in supported_chars:
                continue

            # random_bg = generate_random_background(bg)
            target_name = PATH_DATA_GENERATED_HANZI / c
            target_name.mkdir(parents=True, exist_ok=True)
            draw_hanzi(c, font, str(target_name / f"{c}_{i}_{j}.png"))
            j += 1


if __name__ == '__main__':
    shutil.rmtree(PATH_DATA_GENERATED_HANZI, ignore_errors=True)
    PATH_DATA_GENERATED_HANZI.mkdir(parents=True, exist_ok=True)

    main()