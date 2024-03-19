import {Injectable} from '@angular/core';
import {BehaviorSubject, Observable} from "rxjs";
import {WebsocketService} from "../websocket-service/websocket.service";
import {Message, MessageType} from "../../example/model/Message";

@Injectable({
  providedIn: 'root'
})
export class ChatService {

  public subject: BehaviorSubject<Message[]> = new BehaviorSubject<Message[]>([]);
  public chat: Observable<Message[]> = this.subject.asObservable();

  constructor(private websocketService: WebsocketService) {
    this.websocketService.connect().subscribe(
      (message: { type: string }) => {
        console.log(message);
        if (message.type === 'ChatMessageChunk') {
          let chunk = message as unknown as { messageId: number, chunk: string };
          this.buildMessage(chunk.messageId, chunk.chunk);
        } else if (message.type === 'ChatMessageFinished') {
          let finished = message as unknown as { messageId: number, imageUrl: string };
          this.addImage(finished.messageId, finished.imageUrl);
        } else if (message.type === 'Error') {
          let error = message as unknown as { errorMessage: string };
          console.error(error.errorMessage);
        }
      },
      err => {
        console.error('Error receiving WebSocket message:', err);
      });
  }

  public sendMessage(message: string) {
    let currentMessages = this.subject.value;
    currentMessages.push({
      id: -1,
      type: MessageType.REQUEST,
      user: 'You',
      waiting: false,
      message: message
    });
    currentMessages.push({
      id: -1,
      type: MessageType.RESPONSE,
      user: 'Z',
      waiting: true,
      message: message
    });
    this.subject.next(currentMessages);
    this.websocketService.sendMessage(message);
  }

  buildMessage(id: number, message: string) {
    let currentMessages = this.subject.value;
    let existingMessage = currentMessages.find((msg) => msg.id === id);
    if (existingMessage) {
      existingMessage.message += message;
    } else {
      currentMessages = currentMessages.filter(m => !m.waiting);
      currentMessages.push({
        id: id,
        type: MessageType.RESPONSE,
        user: 'Zühlki assistant',
        waiting: false,
        message: message
      });
    }
    this.subject.next(currentMessages);
  }

  addImage(id: number, imageUrl: string) {
    let currentMessages = this.subject.value;
    let existingMessage = currentMessages.find((msg) => msg.id === id);
    if (existingMessage) {
      existingMessage.imageUrl = imageUrl;
      this.subject.next(currentMessages);
    }
  }
}
