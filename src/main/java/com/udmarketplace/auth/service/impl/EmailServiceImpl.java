package com.udmarketplace.auth.service.impl;

import com.udmarketplace.auth.model.User;
import com.udmarketplace.auth.service.EmailService;
import com.udmarketplace.auth.service.PythonEmailClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final PythonEmailClientService pythonEmailClient;

    @Override
    public void sendTwoFactorCode(User user, String code, int expirationMinutes) {
        pythonEmailClient.enviarCodigo2FA(user, code, expirationMinutes);
    }
}
