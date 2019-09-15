from collections import defaultdict
from typing import Dict

from yangtao.config import PATH_ETYMOLOGY


def parse_etymology() -> Dict[str, str]:
    """ Parses the Etymological Dictionary of Han/Chinese Characters by Lawrence J. Howell """
    with open(PATH_ETYMOLOGY, encoding="utf-8") as f:

        buffer = defaultdict(list)
        for line in f:
            line = line.strip()

            # Skip empty lines
            if not line:
                continue

            # New block
            if line[1] == "　" and line[2] == "(":
                hanzi = line[0]
            else:
                buffer[hanzi].append(line)

        result = {}
        for k,v in buffer.items():
            result[k] = " ".join(v)

    return result


if __name__ == '__main__':
    parse_etymology()