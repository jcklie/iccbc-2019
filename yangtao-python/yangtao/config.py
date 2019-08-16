from pathlib import Path

PATH_ROOT: Path = Path(__file__).resolve().parents[1]
PATH_DATA: Path = PATH_ROOT / "data"
PATH_DATA_EXTERN: Path = PATH_DATA / "external"
PATH_DATA_PROCESSED: Path = PATH_DATA / "processed"
PATH_DATA_GENERATED: Path = PATH_DATA / "generated"
PATH_DATA_RESULTS: Path = PATH_DATA / "results"

PATH_DATA_SVG_RAW: Path = PATH_DATA_EXTERN / "graphics.txt"
PATH_DATA_SVG: Path = PATH_DATA_PROCESSED / "svg"

PATH_DATA_FONTS: Path = PATH_DATA_EXTERN / "fonts"
PATH_DATA_CHARACTER_FREQUENCY: Path = PATH_DATA_EXTERN / "the1stthousand.txt"
PATH_DATA_BACKGROUNDS: Path = PATH_DATA_EXTERN / "backgrounds"
PATH_DATA_GENERATED_HANZI: Path = PATH_DATA_GENERATED / "hanzi"

PATH_CTW_RAW: Path = Path(r"E:\yangtao\ctw\raw\img")
PATH_CTW_PATCHES_TRAIN: Path = Path(r"E:\yangtao\ctw\patches\train")
PATH_CTW_ANNOTATIONS_TRAIN = Path(r"E:\yangtao\ctw\train.jsonl")