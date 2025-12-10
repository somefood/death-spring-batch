# 청크 지향 처리

## Chunk 처리 실행 과정

1. 데이터 읽기 (ItemReader): 데이터를 하나씩 읽어들임. Chunk 크기만큼 반복 실행됨
2. 데이터 가공(깎기) (ItemProcessor): 읽어들인 데이터를 원하는 대로 가공하거나 필요없으면 필터링
3. 데이터 저장 (ItemWriter): Chunk 단위로 모아진 데이터를 한 번에 처리. DB나 파일 원하는 곳에

## 파일 기반 배치 처리의 시작: FlatFileItemReader와 FlatFileItemWriter

- 플랫 파일이란?
  - 단순하게 행과 열로만 구성된 파일
  - XML처럼 복잡한 구조 따윈 없고, CSV 파일같이 단순함
  - 각 라인이 하나의 데이터임
    - 한 줄이 독립적인 레코드로 취급됨. 줄 바꿈(\n)이 레코드의 끝을 의미함
    - 구분자나 고정 길이로 각 필드를 구분할 수 있음
  - 다양한 구분자 지원
    - CSV: 쉼표(,)로 각 필드를 구분함.
    - TSV: 탭(\t)으로 구분하며, 쉼표가 데이터에 포함될 때 유용함
    - 고정 길이: 각 필드가 정해진 길이를 가짐. 레거시 시스템에서 주로 사용
  - 강력한 호환성과 범용성
    - 거의 모든 시스템에서 읽고 쓸 수 있는 표준 형식
    - 엑셀, 데이터베이스 등 다양한 도구와 호환됨
    - 사람도 읽기 쉬움
    - 디버깅이 용이하며, 대용량 데이터 처리에도 적합한 단순 구조
- FlatFilterItemReader
  - `read()` 메서드의 핵심 동작
    - 파일에서 한 줄을 읽어옴
    - 읽어온 한 줄의 문자열을 우리가 사용할 객체로 변환해 리턴함
    ```java
    // FlatFileItemReader.doRead()
    ...
    String line = readLine(); // 한 줄의 데이터를 읽어온다.
    ...
    // 문자열을 도메인 객체로 변환해 리턴한다. 
    return lineMapper.mapLine(line, lineCount); 
    ```
  - LineMapper라는 컴포넌트가 한 줄의 데이터를 객체로 변환시켜줌
  - 실전에서는 `DefaultLineMapper` 구현체로 대부분 해결해줌
    - Tokenization: 하나의 문자열 라인을 토큰 단위로 분리함
    - 분리된 토큰들을 도메인 객체의 프로퍼티에 매핑함
    ```java
    public interface LineMapper<T> {
        T mapLine(String line, int lineNumber) throws Exception;
    }
    ```

1. FlatFileItemReader는 파일의 한 줄을 읽어 객체로 변환한다. 여기서 파일의 한 줄을 객체로 변환하는 역할은 DefaultLineMapper가 담당한다.
2. DefaultLineMapper는 LineTokenizer를 사용해 문자열을 각 필드로 토큰화하고, 토큰화 결과인 FieldSet을 FieldSetMapper에 전달한다.
3. FieldSetMapper의 기본 구현체인 BeanWrapperFieldSetMapper는 FieldSet에 지정된 필드 이름과 매핑할 객체의 프로퍼티 이름을 매핑해 최종적으로 우리가 사용할 객체를 생성한다. 이때 객체의 setter 메서드를 사용한다.