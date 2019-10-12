import pinyin

from yangtao.config import PATH_AUDIO
from yangtao.hanzi import get_most_frequent_characters
from yangtao.util import download_file, setup_logging

URL_ROOT = "https://www.mdbg.net/chinese/rsc/audio/voice_pinyin_pz/{0}.mp3"


if __name__ == '__main__':
    """ Download pronunciation mp3s from the internet. """
    setup_logging()

    PATH_AUDIO.mkdir(parents=True, exist_ok=True)

    for c in get_most_frequent_characters():
        pronounciation = pinyin.get(c, format="numerical")
        pronounciation = pronounciation.replace("v", "uu")

        if pronounciation == "嗯":
            pronounciation = "en1"

        url = URL_ROOT.format(pronounciation)
        target_path = PATH_AUDIO / f"{pronounciation}.mp3"
        download_file(url, target_path)

