package dev.ancaghenade.testcontainerslambda;


import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MessageController {

  private final MessageService messageService;

  @Autowired
  public MessageController(MessageService messageService) {
    this.messageService = messageService;
  }

  @GetMapping(
      path = "/{message}", produces = MediaType.TEXT_PLAIN_VALUE)
  public String uploadShipmentImage(@PathVariable("message") String message) throws IOException {
    return messageService.invokeLambda(message);
  }
}
