package com.company.birthday.controller;

import com.company.birthday.dto.request.MessageTemplateRequest;
import com.company.birthday.dto.response.MessageTemplateResponse;
import com.company.birthday.dto.response.PaginationView;
import com.company.birthday.entity.MessageTemplateType;
import com.company.birthday.service.MessageTemplateService;
import com.company.birthday.service.exception.DuplicateFieldException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class MessageTemplateController {

    private final MessageTemplateService messageTemplateService;

    public MessageTemplateController(MessageTemplateService messageTemplateService) {
        this.messageTemplateService = messageTemplateService;
    }

    @SuppressWarnings("SpringMVCViewInspection")
    @GetMapping("/messages")
    public String messageList(
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            Model model
    ) {
        loadMessagePage(model, pageable);
        model.addAttribute("openMessageModal", false);
        model.addAttribute("modalMode", ModalMode.CREATE);
        model.addAttribute("messageFormAction", "/messages");
        model.addAttribute("messageForm", new MessageTemplateRequest());
        return "messageList/message-list";
    }

    @PostMapping("/messages")
    public String createMessage(
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @Valid @ModelAttribute("messageForm") MessageTemplateRequest messageForm,
            BindingResult bindingResult,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            return handleFormError(model, pageable, ModalMode.CREATE, "/messages");
        }

        try {
            messageTemplateService.createMessage(messageForm);
            return "redirect:/messages";
        } catch (DuplicateFieldException ex) {
            bindingResult.rejectValue(ex.getFieldName(), "", ex.getMessage());
        } catch (EntityNotFoundException ex) {
            bindingResult.reject("message.notFound", ex.getMessage());
        }

        return handleFormError(model, pageable, ModalMode.CREATE, "/messages");
    }

    @PostMapping("/messages/{messageId}")
    public String updateMessage(
            @PathVariable Integer messageId,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @Valid @ModelAttribute("messageForm") MessageTemplateRequest messageForm,
            BindingResult bindingResult,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            return handleFormError(model, pageable, ModalMode.UPDATE, "/messages/" + messageId);
        }

        try {
            messageTemplateService.updateMessage(messageId, messageForm);
            return "redirect:/messages";
        } catch (DuplicateFieldException ex) {
            bindingResult.rejectValue(ex.getFieldName(), "", ex.getMessage());
        } catch (EntityNotFoundException ex) {
            bindingResult.reject("message.notFound", ex.getMessage());
        }

        return handleFormError(model, pageable, ModalMode.UPDATE, "/messages/" + messageId);
    }

    private void loadMessagePage(Model model, Pageable pageable) {
        Page<MessageTemplateResponse> messagePage = messageTemplateService.getMessages(pageable);

        model.addAttribute("messages", messagePage.getContent());
        model.addAttribute("messageTypes", MessageTemplateType.values());
        model.addAttribute("pagination", PaginationView.from(messagePage));
        model.addAttribute("baseUrl", "/messages");
    }

    private String handleFormError(Model model, Pageable pageable, ModalMode mode, String formAction) {
        loadMessagePage(model, pageable);
        model.addAttribute("openMessageModal", true);
        model.addAttribute("modalMode", mode);
        model.addAttribute("messageFormAction", formAction);
        return "messageList/message-list";
    }
}


