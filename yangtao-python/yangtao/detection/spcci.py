from dataclasses import dataclass
from pathlib import Path
from struct import *
from typing import Dict

import numpy as np
from PIL import Image
from tqdm import tqdm

from yangtao.config import *
from yangtao.hanzi import get_most_frequent_characters


@dataclass
class CcbHeader:
    number_of_characters: int
    index_table_offset: int
    font_name: str
    tag: str

    def __post_init__(self):
        self.font_name = self.font_name.decode("GB2312").strip("\x00")
        self.tag = self.tag.decode("GB2312").strip("\x00")


def parse_ccb_header(buffer) -> CcbHeader:
    format = "II50s10s"
    return CcbHeader(*unpack_from(format, buffer))


def parse_ccb_index(buffer, header: CcbHeader) -> Dict[str, int]:
    offset = 512
    fmt = "HI"

    result = {}

    for i in range(header.number_of_characters):
        character, _ = unpack_from(fmt, buffer, offset)
        character = character.to_bytes(2, byteorder='big')
        character = character.decode("GBK").strip().strip("\x00")

        result[character] = i

        offset += 6


    return  result

def extract_ccb_glyph(buffer, header: CcbHeader, character: str, idx: int, target_folder: Path):
    pixel_offset = 512 + header.number_of_characters * 6 + idx * 64 * 64

    pixels = np.frombuffer(buffer, dtype=np.uint8, count=(64 * 64), offset=pixel_offset).reshape((64, 64)).copy()
    assert pixels[0, 0] == pixels[-1, -1]

    background_color = pixels[-1, -1]
    pixels[pixels == background_color] = 255

    im = Image.fromarray(pixels)
    # im = im.convert('RGB')

    # im = im.resize((96, 96), Image.ANTIALIAS)

    folder = target_folder / character
    folder.mkdir(exist_ok=True, parents=True)
    target = folder / f"{header.font_name}_{character}.png"
    im.save(str(target), "png")


def convert_ccb(source_folder: Path, target_folder: Path):
    ccb_files = list(source_folder.iterdir())

    for path_to_ccb in tqdm(ccb_files):

        with open(path_to_ccb, "rb") as f:
            buffer = f.read()

        header = parse_ccb_header(buffer)

        indices = parse_ccb_index(buffer, header)

        for character in  get_most_frequent_characters(100):
            extract_ccb_glyph(buffer, header, character, indices[character], target_folder)


def main():
    convert_ccb(PATH_DATA_RAW_SPCCI_120_TRAIN, PATH_DATA_GENERATED_SPCCI_120_TRAIN)
    convert_ccb(PATH_DATA_RAW_SPCCI_120_TEST, PATH_DATA_GENERATED_SPCCI_120_TEST)
    convert_ccb(PATH_DATA_RAW_SPCCI_280_TRAIN, PATH_DATA_GENERATED_SPCCI_280_TRAIN)
    convert_ccb(PATH_DATA_RAW_SPCCI_280_TEST, PATH_DATA_GENERATED_SPCCI_280_TEST)



if __name__ == '__main__':
    main()