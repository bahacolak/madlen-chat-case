package com.madlen.chat.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Chat mesajı göndermek için kullanılan request DTO.
 * 
 * NOT: Bu bir case study projesi olduğu için max length validation eksik.
 * Production ortamında güvenlik riski oluşturur çünkü:
 * - DoS (Denial of Service) saldırıları: Çok uzun string'ler gönderilerek
 *   sistem kaynaklarını tüketebilir (memory, database, API quota)
 * - Database overflow: Çok uzun mesajlar database column limit'lerini aşabilir
 * - API quota abuse: Uzun mesajlar external API'lere gönderilerek quota tüketilebilir
 * 
 * Production'da şu şekilde validation eklenmeli:
 * 
 * @Size(max = 10000, message = "Message cannot exceed 10000 characters")
 * @NotBlank(message = "Message is required")
 * private String message;
 * 
 * @Size(max = 200, message = "Model name cannot exceed 200 characters")
 * @NotBlank(message = "Model is required")
 * private String model;
 * 
 * @Size(max = 10485760, message = "Image size cannot exceed 10MB") // Base64 için yaklaşık limit
 * private String image;
 */
@Data
public class ChatRequest {
    @NotBlank(message = "Message is required")
    private String message;

    @NotBlank(message = "Model is required")
    private String model;

    private Long conversationId; // Optional: if not provided, a new conversation will be created
    
    private String image; // Optional: base64 encoded image (data:image/jpeg;base64, prefix is optional)
}

