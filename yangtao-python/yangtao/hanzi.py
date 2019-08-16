from typing import List

from yangtao.config import PATH_DATA_CHARACTER_FREQUENCY


def get_most_frequent_characters(n: int) -> List[str]:
    result = []
    with open(PATH_DATA_CHARACTER_FREQUENCY) as f:
        i = 1
        for line in f:

            parts = line.split()
            result.append(parts[1])

            if i >= n:
                break

            i += 1
    return result