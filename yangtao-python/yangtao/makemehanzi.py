import json
import logging
from dataclasses import dataclass
from pathlib import Path
from typing import List

import wget

from yangtao.util import setup_logging
from yangtao.config import PATH_DATA_MAKE_ME_A_HANZI_SVG_RAW, PATH_DATA_SVG, PATH_DATA_MAKE_ME_A_HANZI_DICTIONARY_RAW

_SVG_URL = "https://raw.githubusercontent.com/skishore/makemeahanzi/master/graphics.txt"
_DICTIONARY_URL = "https://raw.githubusercontent.com/skishore/makemeahanzi/master/dictionary.txt"


@dataclass
class Decomposition:
    indices: List
    tree: List

    def get_character_for_stroke(self, stroke_number: int):
        cur = self.tree
        indices = self.indices[stroke_number]
        if indices is None:
            return None

        for idx in indices:
            cur = cur[idx]

        return cur[0]


def _download_file(url: str, target_path: Path):
    import ssl

    if target_path.exists():
        logging.info("File already exists: [%s]", str(target_path.resolve()))
        return

    wget.download(url, str(target_path.resolve()))


def parse_decomposition_data():
    def _parse(tokens: List[str]):
        if len(tokens) == 0:
            return []

        s = tokens.pop(0)

        if s in "⿰⿱⿴⿵⿻⿶⿸⿹⿺":
            p1 = _parse(tokens)
            p2 = _parse(tokens)
            return [p1, p2]
        elif s in "⿲⿳⿷":
            p1 = _parse(tokens)
            p2 = _parse(tokens)
            p3 = _parse(tokens)
            return [p1, p2, p3]
        else:
            return [s]

    decompositions = {}
    with open(PATH_DATA_MAKE_ME_A_HANZI_DICTIONARY_RAW) as f:
        for line in f:
            obj = json.loads(line)
            tree = _parse(list(obj["decomposition"]))
            decomposition = Decomposition(obj["matches"], tree)
            decompositions[obj["character"]] = decomposition

    return decompositions


def generate_svg(decompositions):
    # We use the makemeahanzi SVG here. The format is one json object per line which contains the
    # SVG paths in addition to some other data

    colors = {
        "月" : "dimgray",
        "火" : "firebrick",
        "艹" : "darkgreen",
        "氵" : "cornflowerblue",
        "土": "saddlebrown",
        "石": "lightslategray",
        "日": "goldenrod"
    }

    with open(PATH_DATA_MAKE_ME_A_HANZI_SVG_RAW) as f:
        for line in f:

            obj = json.loads(line)
            character = obj['character']
            strokes = obj["strokes"]

            decomposition = decompositions[character]
            assert len(strokes) == len(decomposition.indices)

            paths = []
            for i, stroke in enumerate(strokes):
                part = decomposition.get_character_for_stroke(i)
                color = colors.get(part, "black")

                path = f'<path fill="{color}" d="{stroke}"></path>'
                paths.append(path)

            path_str = "\n".join(paths)
            svg = f"""<svg viewBox="0 0 1024 1024">
              <g transform="scale(1, -1) translate(0, -900)">
                {path_str}
              </g>
            </svg>
            """

            with open(PATH_DATA_SVG / f"{character}.svg", "w") as f_out:
                f_out.write(svg)

if __name__ == '__main__':
    setup_logging()
    PATH_DATA_MAKE_ME_A_HANZI_SVG_RAW.parent.mkdir(parents=True, exist_ok=True)
    PATH_DATA_SVG.mkdir(parents=True, exist_ok=True)

    _download_file(_SVG_URL, PATH_DATA_MAKE_ME_A_HANZI_SVG_RAW)
    _download_file(_DICTIONARY_URL, PATH_DATA_MAKE_ME_A_HANZI_DICTIONARY_RAW)

    decompositions = parse_decomposition_data()

    generate_svg(decompositions)
