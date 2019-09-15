# coding=utf-8
from typing import List

from yangtao.config import PATH_DATA_CHARACTER_FREQUENCY, NUMBER_OF_CHARACTERS
from yangtao.util import _download_file, setup_logging


def get_most_frequent_characters() -> List[str]:
    """Parses the list of most frequent characters and returns the first N as a list."""
    URL = r"https://pastebin.com/raw/vCB6K1M6"
    _download_file(URL, PATH_DATA_CHARACTER_FREQUENCY)

    # If this download does not work, dowload
    # https://www.ugent.be/pp/experimentele-psychologie/en/research/documents/subtlexch/subtlexchchr.zip
    # open the Excel and copy everything below the header to the specified file.
    # The first line should be
    # 我	2058980	43956,7	6,3137	6242	99,98	3,7953

    result = []
    n = NUMBER_OF_CHARACTERS
    with open(PATH_DATA_CHARACTER_FREQUENCY, encoding="utf-8") as f:
        i = 1
        for line in f:

            parts = line.split()
            result.append(parts[0])

            if i >= n:
                break

            i += 1
    return result


if __name__ == '__main__':
    setup_logging()
    l = get_most_frequent_characters()
    x = "一三上下不与世业东两个中为主么义之也了事二于些产人什从他代以们件任会但位体何作你使信做儿先入全公关其内再军几出分利别到制前力加务动化十原去又及反发受变口只可各合同名后向员和四回因国在地场声处外多大天太头女她好如子学它安定实家对将小少尔就工己已常平年并应度建开当很得心必性总情想意感成我或战所手才打把报接提政教数文斯新方无日时明是更最月有本机条来果样次正此比民气水没法活海点然物特现理生用由电的目相看真着知神种立第等系经结给美老者而能自行表被西要见解认论话说走起身过还这进通道那部都里重量长门问间面题高"

    print(a - b)

