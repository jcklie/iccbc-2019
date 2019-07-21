from pathlib import Path

PATH_ROOT: Path = Path(__file__).resolve().parents[1]
PATH_DATA: Path = PATH_ROOT / "data"
PATH_DATA_EXTERN: Path = PATH_DATA / "external"
PATH_DATA_PROCESSED: Path = PATH_DATA / "processed"

PATH_DATA_SVG_RAW: Path = PATH_DATA_EXTERN / "graphics.txt"
PATH_DATA_SVG: Path = PATH_DATA_PROCESSED / "svg"
