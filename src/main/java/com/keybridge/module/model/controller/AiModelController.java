package com.keybridge.module.model.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keybridge.common.api.ApiResponse;
import com.keybridge.common.exception.BusinessException;
import com.keybridge.module.model.entity.AiModel;
import com.keybridge.module.model.service.AiModelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/models")
@RequiredArgsConstructor
public class AiModelController {

    private final AiModelService modelService;

    @GetMapping
    public ApiResponse<Page<AiModel>> list(@RequestParam(defaultValue = "1") long page,
                                           @RequestParam(defaultValue = "10") long size) {
        return ApiResponse.success(modelService.lambdaQuery()
                .orderByDesc(AiModel::getCreatedAt).page(new Page<>(page, size)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<AiModel> create(@Valid @RequestBody AiModel model) {
        model.setId(null);
        if (model.getModelType() == null) model.setModelType("CHAT");
        if (model.getStatus() == null) model.setStatus(1);
        modelService.save(model);
        return ApiResponse.success(model);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<AiModel> update(@PathVariable Long id, @Valid @RequestBody AiModel model) {
        if (modelService.getById(id) == null) throw new BusinessException(404, "模型不存在");
        model.setId(id);
        modelService.updateById(model);
        return ApiResponse.success(modelService.getById(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        if (!modelService.removeById(id)) throw new BusinessException(404, "模型不存在");
        return ApiResponse.success();
    }
}
