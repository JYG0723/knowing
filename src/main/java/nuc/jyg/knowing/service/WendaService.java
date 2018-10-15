package nuc.jyg.knowing.service;

import org.springframework.stereotype.Service;

/**
 * @author Ji YongGuang.
 * @date  2017/11/10.
 */
@Service
public class WendaService {
    public String getMessage(int userId) {
        return "Hello Message:" + String.valueOf(userId);
    }
}
