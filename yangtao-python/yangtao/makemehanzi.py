import json
import logging
from pathlib import Path

import wget

from yangtao.util import setup_logging
from yangtao.config import PATH_DATA_SVG_RAW, PATH_DATA_SVG

_SVG_URL = "https://raw.githubusercontent.com/skishore/makemeahanzi/master/graphics.txt"


def _download_file(url: str, target_path: Path):
    import ssl

    if target_path.exists():
        logging.info("File already exists: [%s]", str(target_path.resolve()))
        return

    wget.download(url, str(target_path.resolve()))


def generate_svg():
    # We use the makemeahanzi SVG here. The format is one json object per line which contains the
    # SVG paths in addition to some other data
    with open(PATH_DATA_SVG_RAW) as f:
        for line in f:
            obj = json.loads(line)

            paths = "\n".join(f'<path d="{stroke}"></path>' for stroke in obj["strokes"])

            svg = f"""<svg viewBox="0 0 1024 1024">
              <g transform="scale(1, -1) translate(0, -900)">
                {paths}
              </g>
            </svg>
            """

            with open(PATH_DATA_SVG / f"{obj['character']}.svg", "w") as f_out:
                f_out.write(svg)


if __name__ == '__main__':
    setup_logging()
    PATH_DATA_SVG_RAW.parent.mkdir(parents=True, exist_ok=True)
    PATH_DATA_SVG.mkdir(parents=True, exist_ok=True)

    _download_file(_SVG_URL, PATH_DATA_SVG_RAW)

    generate_svg()
