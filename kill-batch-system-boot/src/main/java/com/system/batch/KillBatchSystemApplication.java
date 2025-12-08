package com.system.batch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class KillBatchSystemApplication {

	public static void main(String[] args) {
        // 배치의 작업 성공/실패 상태를 exit code로 외부 시스템에 전달할 수 있어 배치 모니터링과 제어에 필수적이라 아래와 같이 exit로 감싸는걸 권장
		System.exit(SpringApplication.exit(SpringApplication.run(KillBatchSystemApplication.class, args)));
	}

}
