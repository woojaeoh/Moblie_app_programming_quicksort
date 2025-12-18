# QuickSort

> AI 기반 스마트 분리수거 도우미

사진 한 장으로 쓰레기를 자동 분류하고, 올바른 분리수거 방법을 안내받으세요.

## 프로젝트 소개

QuickSort는 AI 이미지 분석을 통해 쓰레기를 자동으로 분류하고, 올바른 분리수거 방법을 안내하는 Android 애플리케이션입니다. 사용자는 분리수거를 통해 절감한 탄소량을 추적하고, 나무를 성장시키는 뿌듯함을 얻을 수 있습니다.

### 주요 기능
- AI 이미지 분석을 통한 쓰레기 자동 분류
- 카테고리별 분리수거 가이드 제공
- 탄소 절감량 추적 및 통계
- 사용자 랭킹 시스템
- 분리수거 기록 관리

## 팀원

| 역할                | 이름 | 담당 업무                                    |
|-------------------|------|------------------------------------------|
| **Frontend**      | 안선우 | 화면 개발 담당, 나무성장 담당                        |
| **AI**            | 이동섭 | 모델 정확도 및 성능 개선                           |
| **Backend_login** | 염경하 | 로그인, 회원가입 기능 개발                          |
| **Backend_main**  | 오재우 | 사진촬영 후 AI서버 통신구조 확립 및 내 통계, 전체적인 백엔드 플로우 |

## 실행 방법

### AI 서버 실행

1. AI 브랜치로 이동
   ```bash
   git checkout ai
   ```

2. PyTorch 설치 (개별 설치 필요)
   ```bash
   pip install torch torchvision
   ```

3. 필요한 패키지 설치
   ```bash
   pip install fastapi uvicorn pydantic
   ```

4. AI 서버 실행
   ```bash
   uvicorn quicksort_main:app --reload --host 0.0.0.0 --port 8000
   ```

### Android 앱 실행

1. Android Studio에서 프로젝트 열기
2. Firebase 설정 (`google-services.json` 필요)
3. Run 버튼 클릭

---

**상세 API 문서**: [API_DOCUMENTATION.md](./API_DOCUMENTATION.md)


### APK파일
'''v1.0 release 파일에 있습니다!

'''
