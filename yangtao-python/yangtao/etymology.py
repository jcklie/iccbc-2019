from collections import defaultdict
from typing import Dict

path = r"C:\Users\klie\Downloads\Etymological_Dictionary_of_Han_Chinese_Characters.txt"


def parse_etymology() -> Dict[str, str]:
    with open(path, encoding="utf-8") as f:

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
            print(result[k])

    return result


if __name__ == '__main__':
    parse_etymology()