from typing import List

from yangtao.config import PATH_DATA_CHARACTER_FREQUENCY, NUMBER_OF_CHARACTERS


def get_most_frequent_characters() -> List[str]:
    result = []
    n = NUMBER_OF_CHARACTERS
    with open(PATH_DATA_CHARACTER_FREQUENCY, encoding="utf-8") as f:
        i = 1
        for line in f:

            parts = line.split()
            result.append(parts[1])

            if i >= n:
                break

            i += 1
    return result