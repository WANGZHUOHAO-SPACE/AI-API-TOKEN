package com.keybridge.module.model.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.keybridge.common.api.ApiResponse;
import com.keybridge.common.exception.BusinessException;
import com.keybridge.module.model.entity.ModelChannel;
import com.keybridge.module.model.service.ModelChannelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/model-channels")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ModelChannelController {

    private final ModelChannelService channelService;

    @GetMapping
    public ApiResponse<Page<ModelChannel>> list(@RequestParam(defaultValue = "1") long page,
                                                 @RequestParam(defaultValue = "10") long size,
                                                 @RequestParam(required = false) Long modelId) {
        return ApiResponse.success(channelService.lambdaQuery()
                .eq(modelId != null, ModelChannel::getModelId, modelId)
                .orderByAsc(ModelChannel::getPriority)
                .page(new Page<>(page, size)));
    }

    @PostMapping
    public ApiResponse<ModelChannel> create(@Valid @RequestBody ModelChannel channel) {
        channel.setId(null);
        if (channel.getPriority() == null) channel.setPriority(0);
        if (channel.getWeight() == null) channel.setWeight(1);
        if (channel.getStatus() == null) channel.setStatus(1);
        channelService.save(channel);
        return ApiResponse.success(channel);
    }

    @PutMapping("/{id}")
    public ApiResponse<ModelChannel> update(@PathVariable Long id, @Valid @RequestBody ModelChannel channel) {
        if (channelService.getById(id) == null) throw new BusinessException(404, "模型渠道不存在");
        channel.setId(id);
        channelService.updateById(channel);
        return ApiResponse.success(channelService.getById(id));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        if (!channelService.removeById(id)) throw new BusinessException(404, "模型渠道不存在");
        return ApiResponse.success();
    }
}
