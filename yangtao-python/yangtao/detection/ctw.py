import shutil
from collections import defaultdict
import json
from pathlib import Path

from PIL import Image

from tqdm import tqdm

from yangtao.config import PATH_CTW_RAW, PATH_CTW_ANNOTATIONS_TRAIN, PATH_CTW_PATCHES_TRAIN
from yangtao.hanzi import get_most_frequent_characters


def read_dataset_description(p: Path):
    counts = defaultdict(int)

    frequent_hanzi = get_most_frequent_characters(10)
    frequent_hanzi = set(frequent_hanzi)

    shutil.rmtree(PATH_CTW_PATCHES_TRAIN, ignore_errors=True)
    i = 0

    with open(p) as f:
        for line in tqdm(f):
            i += 1
            # if i > 1000: break

            entry = json.loads(line)

            path_to_image = PATH_CTW_RAW / f"{entry['image_id']}.jpg"
            original_image = Image.open(path_to_image)

            for group in entry["annotations"]:
                for annotation in group:
                    attrs = annotation["attributes"]
                    if not annotation["is_chinese"] or "occluded" in attrs or "bgcomplex" in attrs:
                        continue

                    text = annotation["text"]
                    if text not in frequent_hanzi:
                        continue

                    assert len(text) == 1, "Expected text to be only one character"

                    top_left_x, top_left_y, h, w = annotation["adjusted_bbox"]

                    if h < 20 or w < 20:
                        continue

                    patch = original_image.crop((top_left_x, top_left_y, top_left_x + h, top_left_y + w))

                    # Resize
                    desired_size = 96
                    patch = patch.resize((desired_size, desired_size))

                    # Save
                    patch_file_name = PATH_CTW_PATCHES_TRAIN / text / f"patch_{text}_{counts[text]:04d}.jpg"
                    patch_file_name.parent.mkdir(parents=True, exist_ok=True)
                    patch.save(patch_file_name)

                    counts[text] += 1

def print_statistics():
    result = { }
    for e in PATH_CTW_PATCHES_TRAIN.iterdir():
        result[e.name] = len(list(e.iterdir()))

    for e in sorted(result.items(), key=lambda kv: kv[1], reverse=True):
        print(e)


if __name__ == '__main__':
    read_dataset_description(PATH_CTW_ANNOTATIONS_TRAIN)
