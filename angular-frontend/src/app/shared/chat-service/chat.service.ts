import {Injectable} from '@angular/core';
import {BehaviorSubject, Observable} from "rxjs";
import {WebsocketService} from "../websocket-service/websocket.service";
import {Message} from "../../example/model/Message";

@Injectable({
  providedIn: 'root'
})
export class ChatService {

  public subject: BehaviorSubject<Message[]> = new BehaviorSubject<Message[]>([]);
  public chat: Observable<Message[]> = this.subject.asObservable();

  constructor(private websocketService: WebsocketService) {
    this.websocketService.connect().subscribe(
      (message: { messageId: number, chunk: string | null, url?: string }) => {
        if (message.chunk !== null) {
          this.buildMessage(message.messageId, message.chunk)
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
      user: 'You',
      waiting: false,
      message: message
    });
    currentMessages.push({
      id: -1,
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
        user: 'Zühlki assistant',
        waiting: false,
        message: message
      });
    }
    this.subject.next(currentMessages);
  }
}
