package com.system.batch.chapter01.tasklet;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import java.util.List;

/**
 * 일일 재고 현황 알림 Tasklet
 * - 매일 오전 8시에 주요 품목 재고 상태를 점검하고 알림 발송
 */
@Slf4j
public class DailyInventoryReportTasklet implements Tasklet {
    private final AlimService alimService;
    private final InventoryRepository inventoryRepository;

    public DailyInventoryReportTasklet(AlimService alimService, InventoryRepository inventoryRepository) {
        this.alimService = alimService;
        this.inventoryRepository = inventoryRepository;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        List<ItemStock> lowStockItems = inventoryRepository.findLowStockItems(10);  // 재고 10개 이하 조회

        if (lowStockItems.isEmpty()) {
            log.info("✅ 모든 품목 재고 안정");
            return RepeatStatus.FINISHED;
        }

        StringBuilder message = new StringBuilder("⚠️ [재고 부족 품목 알림]\n");
        for (ItemStock item : lowStockItems) {
            message.append(String.format("- %s: 재고 %d개\n", item.getItemName(), item.getStock()));
        }

        log.info("📦 재고 부족 리포트 발송");
        alimService.send(message.toString());
        return RepeatStatus.FINISHED;
    }

    private class AlimService {
        public void send(String string) {
        }
    }

    private class InventoryRepository {
        public List<ItemStock> findLowStockItems(int i) {
            return null;
        }
    }

    private class ItemStock {
        public Object getItemName() {
            return null;
        }

        public Object getStock() {
            return null;
        }
    }
}

