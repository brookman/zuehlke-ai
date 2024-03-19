import {Component, OnInit} from '@angular/core';
import {DataViewModule} from 'primeng/dataview';
import {RatingModule} from "primeng/rating";
import {NgClass, NgForOf, NgOptimizedImage} from "@angular/common";
import {FormsModule} from "@angular/forms";
import {TagModule} from "primeng/tag";
import {ButtonModule} from "primeng/button";
import {LoadingPageComponent} from "../../shared/loading-page/loading-page.component";
import {AvatarModule} from 'primeng/avatar';
import {ChatService} from "../../shared/chat-service/chat.service";
import {Message, MessageType} from '../model/Message';
import {ImageModule} from "primeng/image";
import {SkeletonModule} from "primeng/skeleton";

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
    AvatarModule,
    ImageModule,
    SkeletonModule
  ],
  templateUrl: './chat-history-component.component.html',
  styleUrl: './chat-history-component.component.scss'
})
export class ChatHistoryComponentComponent implements OnInit {

  constructor(private chatService: ChatService) {
  }

  public chatMessages: Message[] = [];

  ngOnInit() {
    this.chatService.chat.subscribe(value => this.chatMessages = value);
  }

  protected readonly MessageType = MessageType;
}









