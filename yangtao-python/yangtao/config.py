from pathlib import Path

PATH_ROOT: Path = Path(__file__).resolve().parents[1]
PATH_DATA: Path = PATH_ROOT / "data"
PATH_DATA_EXTERN: Path = PATH_DATA / "external"
PATH_DATA_GENERATED: Path = PATH_DATA / "generated"
PATH_DATA_RESULTS: Path = PATH_DATA / "results"

PATH_DATA_MAKE_ME_A_HANZI_SVG_RAW: Path = PATH_DATA_EXTERN / "graphics.txt"
PATH_DATA_MAKE_ME_A_HANZI_DICTIONARY: Path = PATH_DATA_EXTERN / "dictionary.txt"
PATH_DATA_SVG: Path = PATH_DATA_GENERATED / "svg"

PATH_DATA_FONTS: Path = PATH_DATA_EXTERN / "fonts"
PATH_DATA_CHARACTER_FREQUENCY: Path = PATH_DATA_EXTERN / "most_common_hanzi.txt"
PATH_DATA_BACKGROUNDS: Path = PATH_DATA_EXTERN / "backgrounds"
PATH_DATA_GENERATED_HANZI: Path = PATH_DATA_GENERATED / "hanzi"

PATH_CTW_RAW: Path = Path(r"E:\yangtao\ctw\raw\img")
PATH_CTW_PATCHES_TRAIN: Path = Path(r"E:\yangtao\ctw\patches\train")
PATH_CTW_ANNOTATIONS_TRAIN = Path(r"E:\yangtao\ctw\train.jsonl")

PATH_SPCCI_ROOT: Path = PATH_DATA_EXTERN / "spcci"
PATH_SPCCI_120: Path = PATH_SPCCI_ROOT / "SCUT-SPCCI_120"
PATH_SPCCI_280: Path = PATH_SPCCI_ROOT / "SCUT-SPCCI_280"

PATH_DATA_RAW_SPCCI_120_TRAIN: Path = PATH_SPCCI_120 / "SCUT-SPCCI_120_trn"
PATH_DATA_RAW_SPCCI_120_TEST: Path = PATH_SPCCI_120 / "SCUT-SPCCI_120_tst"
PATH_DATA_RAW_SPCCI_280_TRAIN: Path = PATH_SPCCI_280 / "SCUT-SPCCI_280_trn"
PATH_DATA_RAW_SPCCI_280_TEST: Path = PATH_SPCCI_280 / "SCUT-SPCCI_280_tst"

PATH_SPCCI_GENERATED: Path = PATH_DATA_GENERATED / "spcci"
PATH_DATA_GENERATED_SPCCI_120_TRAIN: Path = PATH_SPCCI_GENERATED / "SCUT-SPCCI_120_trn"
PATH_DATA_GENERATED_SPCCI_120_TEST: Path = PATH_SPCCI_GENERATED / "SCUT-SPCCI_120_tst"
PATH_DATA_GENERATED_SPCCI_280_TRAIN: Path = PATH_SPCCI_GENERATED / "SCUT-SPCCI_280_trn"
PATH_DATA_GENERATED_SPCCI_280_TEST: Path = PATH_SPCCI_GENERATED / "SCUT-SPCCI_280_tst"

PATH_DATA_GENERATED_SPCCI_120_HOG: Path = PATH_DATA_GENERATED / "SCUT-SPCCI_120_trn.pkl"
PATH_DATA_GENERATED_SPCCI_280_HOG: Path = PATH_DATA_GENERATED / "SCUT-SPCCI_280_trn.pkl"
PATH_DATA_GENERATED_SPCCI_280_HOG_TMP: Path = PATH_DATA_GENERATED / "SCUT-SPCCI_280_tmp.pkl"
PATH_DATARESULT_SPCCI_HOG: Path = PATH_DATA_RESULTS / "hog_model.h5"
PATH_DATARESULT_SPCCI_HOG_TMP: Path = PATH_DATA_RESULTS / "hog_model.h5"

PATH_DATARESULT_SPCCI_HOG_LABELS: Path = PATH_DATA_RESULTS / "labels.txt"

PATH_GENERATED_DICTIONARY = PATH_DATA_GENERATED / "dictionary.tsv"
PATH_ETYMOLOGY = PATH_DATA_EXTERN / "Etymological_Dictionary_of_Han_Chinese_Characters.txt"

PATH_AUDIO: Path = PATH_DATA_EXTERN / "audio"


NUMBER_OF_CHARACTERS = 250
