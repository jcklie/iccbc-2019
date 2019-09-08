import json
import logging
from pathlib import Path
from typing import List, Dict

import wget

import attr

import webcolors

from yangtao.hanzi import get_most_frequent_characters
from yangtao.util import setup_logging
from yangtao.config import PATH_DATA_MAKE_ME_A_HANZI_SVG_RAW, PATH_DATA_SVG, PATH_DATA_MAKE_ME_A_HANZI_DICTIONARY, \
    PATH_GENERATED_DICTIONARY

_SVG_URL = "https://raw.githubusercontent.com/skishore/makemeahanzi/master/graphics.txt"
_DICTIONARY_URL = "https://raw.githubusercontent.com/skishore/makemeahanzi/master/dictionary.txt"


@attr.s
class DictionaryEntry:
    character: str = attr.ib()
    pinyin: str = attr.ib()
    definition: str = attr.ib()
    decomposition: str = attr.ib()
    origin: str = attr.ib()
    phonetic: str = attr.ib()
    semantic: str = attr.ib()
    hint: str = attr.ib()
    indices: List = attr.ib()
    tree: List = attr.ib()

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


def parse_dictionary():
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

    result = {}
    with open(PATH_DATA_MAKE_ME_A_HANZI_DICTIONARY, encoding="utf-8") as f:
        for line in f:
            obj = json.loads(line)
            tree = _parse(list(obj["decomposition"]))
            character = obj["character"]
            pinyin = ", ".join(obj["pinyin"])
            definition = str(obj.get("definition", ""))
            decomposition = obj["decomposition"]
            origin = obj["etymology"]["type"] if "etymology" in obj else ""

            if "etymology" in obj:
                phonetic = obj["etymology"]["phonetic"] if "phonetic" in obj["etymology"] else ""
                semantic = obj["etymology"]["semantic"] if "semantic" in obj["etymology"] else ""
                hint = obj["etymology"]["hint"] if "hint" in obj["etymology"] else ""
            else:
                phonetic = ""
                semantic = ""
                hint = ""

            entry = DictionaryEntry(character, pinyin, definition, decomposition, origin, phonetic, semantic, hint, obj["matches"], tree)
            result[obj["character"]] = entry

    with open(PATH_GENERATED_DICTIONARY, "w", encoding="utf-8") as f:
        target = set(get_most_frequent_characters())
        for e in result.values():
            if e.character not in target:
                continue

            line = f"{e.character}\t{e.pinyin}\t{e.definition}\t{e.decomposition}\t{e.origin}\t{e.phonetic}\t{e.semantic}\t{e.hint}"
            f.write(line)
            f.write("\n")


    return result


def generate_svg(dictionary: Dict[str, DictionaryEntry]):
    # We use the makemeahanzi SVG here. The format is one json object per line which contains the
    # SVG paths in addition to some other data

    colors = {
        "口": "#e35d6a",
        "水": "#40a4df",
        "氵": "#40a4df",
        "木": "#533118",
        "人": "#eec1ad",
        "亻": "#eec1ad",
        "手": "#eec1ad",
        "扌": "#eec1ad",
        "心": "#f7347a ",
        "忄": "#f7347a ",
        "⺗": "#f7347a ",
        "言": "#4156C5",
        "讠": " #4156C5",
        "日": "#EF8E38",
        "糸": "#B7A99B",
        "幺": "#B7A99B",
        "肉": "#E8B3B9 ",
        "月": "#CACACA ",
        "土": "#a0522d",
        "⻌": "#2F2F2F",
        "艹": "#608038",
        "艸": "#608038",
        "草": "#608038",
        "宀": "#A44A4A",
        "貝": " #FFF5EE",
        "贝": " #FFF5EE",
        "女": "#7F00FF",
        "金": "#DAA520",
        "钅": " #DAA520",
        "田": " #C7A54E",
        "火": " #e25822 ",
        "灬": " #e25822 ",
        "石": " #95948B",
        "禾": " #F5DEB3",
        "礻": " #569199 ",
        "示": "  #569199 ",
    }

    WEBCOLORS = list(webcolors.CSS3_NAMES_TO_HEX.values())
    def _get_color(s: str):
        i = ord(s)
        return WEBCOLORS[i % len(WEBCOLORS)]

    with open(PATH_DATA_MAKE_ME_A_HANZI_SVG_RAW, encoding="utf-8") as f:
        for line in f:
            obj = json.loads(line)
            character = obj['character']
            strokes = obj["strokes"]

            entry = dictionary[character]
            assert len(strokes) == len(entry.indices)

            paths = []
            for i, stroke in enumerate(strokes):
                part = entry.get_character_for_stroke(i)

                color = colors.get(part, _get_color(part) if part else "black")

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
    _download_file(_DICTIONARY_URL, PATH_DATA_MAKE_ME_A_HANZI_DICTIONARY)

    dictionary = parse_dictionary()
    generate_svg(dictionary)

