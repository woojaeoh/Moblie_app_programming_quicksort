import re

# 파일 읽기
with open('app/src/main/java/com/example/quicksort/ResultScreen.kt', 'r', encoding='utf-8') as f:
    content = f.read()

# 패턴 찾기 및 수정
old_pattern = r'(            // 설명 \+ 체크박스 목록\n            descriptions\.forEachIndexed \{ index, desc ->\n                Row\('
new_pattern = r'\1                if (index < checkedStates.size) {\n                    Row('

content = re.sub(old_pattern, new_pattern, content)

# 닫는 괄호 추가
old_end = r'(                    \)\n                \}\n            \}\n        \}\n\n        // 하단 버튼)'
new_end = r'\1\n                }\n            }\n        }\n\n        // 하단 버튼)'

# 실제로는 더 간단하게 수정
lines = content.split('\n')
new_lines = []
for i, line in enumerate(lines):
    new_lines.append(line)
    if i == 131 and 'descriptions.forEachIndexed' in line:
        # 다음 줄에 if 문 추가
        indent = '                '
        new_lines.append(indent + 'if (index < checkedStates.size) {')
        # Row 라인은 이미 추가됨

# 파일 쓰기
with open('app/src/main/java/com/example/quicksort/ResultScreen.kt', 'w', encoding='utf-8') as f:
    f.write('\n'.join(new_lines))

print("파일 수정 완료")
