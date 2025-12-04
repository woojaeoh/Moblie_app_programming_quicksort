from transformers import pipeline
from PIL import Image
import requests
import json

ai_model = pipeline("image-to-text", model="Salesforce/blip-image-captioning-base")

CLASS_ORDER = [
    "가구류", "고철류", "나무류", "도기류", "비닐류",
    "스티로폼류", "유리병", "의류", "자전거", "전자제품",
    "종이류", "캔류", "페트병", "플라스틱류", "형광등"
]

WASTE_CLASSES = {
    "가구류": {
        "keywords": [
            "furniture", "sofa", "couch", "loveseat", "armchair",
            "chair", "stool", "bench",
            "table", "dining table", "coffee table", "side table", "nightstand",
            "desk", "office desk", "work desk",
            "bed", "bunk bed", "double bed", "single bed",
            "dresser", "drawer", "chest of drawers",
            "cabinet", "wardrobe", "closet", "cupboard",
            "bookshelf", "shelf", "bookcase",
            "vanity", "makeup table",
            "sink", "kitchen sink",
            "furnishing", "household furniture"
        ],
        "details": {
            "sofa": "01.소파",
            "couch": "01.소파",
            "loveseat": "01.소파",
            "sofa chair": "01.소파",
            "armchair": "01.소파",
            "settee": "01.소파",

            "chair": "02.의자",
            "stool": "02.의자",
            "rocking chair": "02.의자",
            "folding chair": "02.의자",
            "seat": "02.의자",

            "cabinet": "03.수납장(책꽂이/장식장/책장)",
            "storage cabinet": "03.수납장(책꽂이/장식장/책장)",
            "cupboard": "03.수납장(책꽂이/장식장/책장)",
            "bookshelf": "03.수납장(책꽂이/장식장/책장)",
            "bookcase": "03.수납장(책꽂이/장식장/책장)",
            "shelf": "03.수납장(책꽂이/장식장/책장)",
            "display cabinet": "03.수납장(책꽂이/장식장/책장)",
            "display shelf": "03.수납장(책꽂이/장식장/책장)",

            "bed": "04.침대",
            "bunk bed": "04.침대",
            "single bed": "04.침대",
            "double bed": "04.침대",
            "queen bed": "04.침대",
            "king bed": "04.침대",
            "bed frame": "04.침대",

            "dresser": "05.서랍장",
            "drawer": "05.서랍장",
            "drawers": "05.서랍장",
            "chest of drawers": "05.서랍장",
            "bureau": "05.서랍장",

            "wardrobe": "06.장롱",
            "closet": "06.장롱",
            "armoire": "06.장롱",
            "clothes closet": "06.장롱",

            "sink": "07.싱크대",
            "kitchen sink": "07.싱크대",
            "counter sink": "07.싱크대",
            "dishwashing sink": "07.싱크대",

            "desk": "08.책상",
            "office desk": "08.책상",
            "work desk": "08.책상",
            "computer desk": "08.책상",
            "study desk": "08.책상",

            "dining table": "09.밥상",
            "dinner table": "09.밥상",
            "kitchen table": "09.밥상",
            "meal table": "09.밥상",
            "low table": "09.밥상",

            "side table": "10.협탁",
            "end table": "10.협탁",
            "nightstand": "10.협탁",
            "bedside table": "10.협탁",

            "vanity": "11.화장대",
            "dressing table": "11.화장대",
            "makeup table": "11.화장대",
            "makeup vanity": "11.화장대",
            "dressing desk": "11.화장대"
        }
    },

    "고철류": {
        "keywords": ["metal", "iron", "steel", "scrap"],
        "details": {
            "metal scrap": "01.고철(공기구/철사/못 등)",
            "nonferrous": "02.비철금속(알루미늄/스테인리스스틸류 등)",
            "hanger": "03.철옷걸이",
            "electric pan": "04.전기프라이팬",
            "pan": "05.프라이팬",
            "kettle": "06.주전자",
            "golf club": "07.골프채"
        }
    },

    "나무류": {
        "keywords": ["wood", "timber", "lumber"],
        "details": {
            "cutting board": "01.도마",
            "ladle": "02.주걱",
            "utensil": "03.주방용품(튀김용 나무젓가락 등)",
            "hanger": "04.나무행거",
            "frame": "05.액자",
            "decor": "06.장식품",
            "packaging": "07.포장재(생활용품/사무/스포츠용품)"
        }
    },

    "도기류": {
        "keywords": ["ceramic", "pottery", "porcelain", "vase", "bowl"],
        "details": {
            "bowl": "01.그릇류",
            "cup": "02.컵",
            "vase": "03.항아리",
            "flower pot": "04.화분",
            "plate stand": "05.받침",
            "bottle": "06.병",
            "ttukbaegi": "07.뚝배기",
            "ornament": "08.장식품",
            "kettle": "09.주전자"
        }
    },

    "비닐류": {
        "keywords": ["vinyl", "plastic bag", "wrap"],
        "details": {
            "aircap": "01.에어캡",
            "packaging": "02.포장재",
            "bag": "03.봉투",
            "refill container": "04.리필용기",
            "snack bag": "05.과자봉지",
            "cover": "06.일회용덮개"
        }
    },

    "스티로폼류": {
        "keywords": ["styrofoam", "foam"],
        "details": {
            "protection foam": "01.보호재",
            "tray": "02.네모 트레이",
            "container": "03.스티로폼 포장용기"
        }
    },

    "유리병": {
        "keywords": ["glass bottle", "jar", "wine bottle", "beer bottle", "soju bottle"],
        "details": {
            "baccas bottle": "01.박카스병",
            "soju bottle": "02.소주병",
            "beer bottle": "03.맥주병",
            "liquor bottle": "04.기타술병",
            "kitchen container": "05.주방용기",
            "drink bottle": "06.음료수병",
            "water bottle": "07.물병"
        }
    },

    "의류": {
        "keywords": ["clothes", "shirt", "pants", "dress", "fabric"],
        "details": {
            "cotton": "01.면의류",
            "other fabric": "02.기타의류",
            "plant fiber": "03.식물성섬유",
            "animal fiber": "04.동물성섬유",
            "synthetic fiber": "05.합성섬유",
            "other synthetic": "06.기타 합성섬유류",
            "top": "07.상의",
            "bottom": "08.하의",
            "outerwear": "09.외투",
            "onepiece": "10.원피스",
            "leggings": "11.레깅스"
        }
    },

    "자전거": {
        "keywords": ["bicycle", "bike"],
        "details": {
            "bicycle": "01.두발자전거",
            "tricycle": "02.세발자전거",
            "four wheel bike": "03.네발자전거"
        }
    },

    "전자제품": {
        "keywords": ["tv", "monitor", "computer", "printer", "radio", "speaker"],
        "details": {
            "tv": "01.TV",
            "refrigerator": "02.냉장고",
            "washing machine": "03.세탁기",
            "air conditioner": "04.에어컨",
            "vending machine": "05.자동판매기",
            "computer": "06.컴퓨터",
            "printer": "07.프린터",
            "copier": "08.복사기",
            "fax": "09.팩시밀리",
            "vacuum": "10.전기청소기",
            "oven": "11.전기오븐",
            "microwave": "12.전자레인지",
            "water purifier": "13.음식물처리기",
            "dryer": "14.식기건조기",
            "air purifier": "15.공기청정기",
            "heater": "16.전기히터",
            "audio": "17.오디오",
            "lamp": "18.전기밥솥",
            "humidifier": "20.가습기",
            "massager": "21.전기다리미",
            "hair dryer": "22.선풍기",
            "mixer": "23.믹서",
            "cleaner": "24.청소기",
            "video player": "25.비디오플레이어",
            "wireless phone": "26.이동전화단말기",
            "hipass": "27.하이패스"
        }
    },

    "종이류": {
        "keywords": ["paper", "cardboard", "carton", "newspaper", "box"],
        "details": {
            "newspaper": "01.신문지",
            "book": "02.책자",
            "notebook": "03.노트",
            "paper bag": "04.종이봉투",
            "box": "05.상자류",
            "packaging box": "06.포장상자",
            "shoe box": "07.신발상자",
            "drink carton": "08.음료수팩"
        }
    },

    "캔류": {
        "keywords": ["can", "tin", "aluminum can", "drink can"],
        "details": {
            "coffee can": "01.커피캔",
            "oil can": "02.참기름캔",
            "drink can": "03.음료수캔",
            "beer can": "04.맥주캔",
            "tin can": "05.통조림캔",
            "spray can": "06.스프레이류"
        }
    },

    "페트병": {
        "keywords": ["water bottle", "bottle of water", "plastic bottle", "pet bottle"],
        "details": {
            "water bottle": "01.페트병",
            "bottle of water": "01.페트병",
            "plastic bottle": "01.페트병",
            "pet bottle": "01.페트병",
            "water cup": "02.일회용 음료수잔"
        }
    },

    "플라스틱류": {
        "keywords": ["plastic", "container", "bucket", "tupperware"],
        "details": {
            "container": "01.밀폐용기",
            "bath item": "02.욕실용품",
            "basket": "03.바구니",
            "toy": "04.장난감",
            "large container": "05.대용량 플라스틱통"
        }
    },

    "형광등": {
        "keywords": ["fluorescent lamp", "light bulb", "tube light"],
        "details": {
            "tube light": "01.직관형",
            "round light": "02.환형",
            "built-in light": "03.안정기내장형",
            "compact light": "04.콤팩트형",
            "incandescent": "05.백열전구",
            "led": "06.LED전구"
        }
    }
}


def classify_class(image_info: str):
    info_low = image_info.lower()

    for cls in CLASS_ORDER:
        for kw in WASTE_CLASSES[cls]["keywords"]:
            if kw in info_low:
                return cls
    return "class 분류 불가"


def classify_details(cls: str, image_info: str):
    if cls not in WASTE_CLASSES:
        return "detail 분류 불가"

    info_low = image_info.lower()
    detail_items = WASTE_CLASSES[cls]["details"]

    sorted_details = sorted(
        detail_items.items(),
        key=lambda x: int(x[1].split('.')[0])
    )

    for kw, detail in sorted_details:
        if kw and kw in info_low:
            return detail

    return "기타"


def classify_image(image_url):
    image = Image.open(requests.get(image_url, stream=True).raw)
    image_info = ai_model(image)[0]["generated_text"]

    cls = classify_class(image_info)
    detail = classify_details(cls, image_info)

    return {
        "status": " ",
        "category": cls,
        "detail": detail
    }


image_url = "https://lottemartzetta.com/images-v3/932dcbc7-fca8-4d43-bcde-f73d1ce3cc7d/f518b743-9ea2-4358-ad18-379bed490ab7/500x500.jpg"
output = classify_image(image_url)

print(json.dumps(output, ensure_ascii=False, indent=2))
