import {Component, OnInit, signal} from '@angular/core';
import {DataViewModule} from 'primeng/dataview';
import {RatingModule} from "primeng/rating";
import {NgClass, NgForOf, NgOptimizedImage} from "@angular/common";
import {FormsModule} from "@angular/forms";
import {TagModule} from "primeng/tag";
import {ButtonModule} from "primeng/button";
import {LoadingPageComponent} from "../../shared/loading-page/loading-page.component";
import { AvatarModule } from 'primeng/avatar';
import {WebsocketService} from "../../shared/websocket-service/websocket.service";
import {takeUntil} from "rxjs";

@Component({
  selector: 'chat-history-component',
  standalone: true,
  imports: [
    DataViewModule,
    RatingModule,
    NgClass,
    FormsModule,
    TagModule,
    ButtonModule,
    NgOptimizedImage,
    NgForOf,
    LoadingPageComponent,
    AvatarModule
  ],
  templateUrl: './chat-history-component.component.html',
  styleUrl: './chat-history-component.component.scss'
})
export class ChatHistoryComponentComponent implements OnInit {
  private currentMessageChunks = "";
  messages = signal<Message[]>([]);



  constructor(private websocketService: WebsocketService) {
  }

  public chatMessages: Message[] = [];

  ngOnInit() {

    this.websocketService.connect().subscribe(
      (message: { messageId: number, chunk: string | null, url?: string }) => {
        if(message.chunk !== null) {
          this.buildMessage(message.messageId, message.chunk)
        }
      },
      err => {
        console.error('Error receiving WebSocket message:', err);
      }
    )
  }

  handleClick() {
    this.chatMessages[this.chatMessages.length - 1].message = "Neuer Text";
  }

  buildMessage(id: number, message: string) {
    let existingMessage = this.chatMessages.find((msg) => msg.id === id);
    if(existingMessage) {
      existingMessage.message += message;
    } else {
      this.chatMessages.push({
        id: id,
        user: 'Zühlki assistant',
        waiting: false,
        message: message
      });
    }
  }
}

interface Message {
  id: number,
  user: string,
  waiting: boolean,
  message?: string
}








