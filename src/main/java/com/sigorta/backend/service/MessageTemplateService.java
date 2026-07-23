package com.sigorta.backend.service;

import com.sigorta.backend.dto.MessageTemplateResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageTemplateService {

    private static final List<MessageTemplateResponse> TEMPLATES = List.of(
            template(
                    1,
                    "Poliçe",
                    "Yenileme Hatırlatması",
                    "SMS",
                    "Sayın [Müşteri Adı], [Poliçe Türü] poliçenizin bitişine az kalmıştır. "
                            + "Yenileme işlemleriniz için bizimle iletişime geçebilirsiniz.",
                    ""
            ),
            template(
                    2,
                    "Poliçe",
                    "Süresi Geçmiş Poliçe",
                    "SMS",
                    "Sayın [Müşteri Adı], [Poliçe Türü] poliçenizin süresi dolmuştur. "
                            + "Yenileme ve yeni teklif seçenekleri için bizimle iletişime geçebilirsiniz.",
                    ""
            ),
            template(
                    3,
                    "Müşteri",
                    "Doğum Günü Kutlaması",
                    "SMS",
                    "Sayın [Müşteri Adı], doğum gününüzü kutlar; sağlıklı, güvenli ve mutlu bir yıl dileriz.",
                    ""
            ),
            template(
                    4,
                    "Kampanya",
                    "Yeni Kampanya Duyurusu",
                    "SMS",
                    "Sayın [Müşteri Adı], size özel sigorta kampanyalarımız hakkında bilgi almak için "
                            + "bize ulaşabilirsiniz.",
                    ""
            ),
            template(
                    5,
                    "Poliçe",
                    "Yenileme Hatırlatması",
                    "EMAIL",
                    "Sayın [Müşteri Adı], [Poliçe Türü] poliçenizin bitişine az kalmıştır. "
                            + "Yenileme işlemleriniz için bizimle iletişime geçebilirsiniz.",
                    "Poliçe Yenileme Hatırlatması"
            ),
            template(
                    6,
                    "Poliçe",
                    "Süresi Geçmiş Poliçe",
                    "EMAIL",
                    "Sayın [Müşteri Adı], [Poliçe Türü] poliçenizin süresi dolmuştur. "
                            + "Yenileme ve yeni teklif seçenekleri için bizimle iletişime geçebilirsiniz.",
                    "Süresi Geçmiş Poliçeniz Hakkında"
            ),
            template(
                    7,
                    "Müşteri",
                    "Doğum Günü Kutlaması",
                    "EMAIL",
                    "Sayın [Müşteri Adı], doğum gününüzü kutlar; sağlıklı, güvenli ve mutlu bir yıl dileriz.",
                    "Doğum Gününüz Kutlu Olsun"
            ),
            template(
                    8,
                    "Kampanya",
                    "Yeni Kampanya Duyurusu",
                    "EMAIL",
                    "Sayın [Müşteri Adı], size özel sigorta kampanyalarımız hakkında bilgi almak için "
                            + "bize ulaşabilirsiniz.",
                    "Size Özel Yeni Sigorta Kampanyamız"
            )
    );

    public List<MessageTemplateResponse> getAllTemplates() {
        return TEMPLATES;
    }

    private static MessageTemplateResponse template(
            long id,
            String category,
            String name,
            String type,
            String content,
            String subject
    ) {
        return new MessageTemplateResponse(id, category, name, type, content, subject);
    }
}
