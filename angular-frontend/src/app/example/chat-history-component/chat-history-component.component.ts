import {Component, OnInit} from '@angular/core';
import {DataViewModule} from 'primeng/dataview';
import {RatingModule} from "primeng/rating";
import {NgClass, NgForOf, NgOptimizedImage} from "@angular/common";
import {FormsModule} from "@angular/forms";
import {TagModule} from "primeng/tag";
import {ButtonModule} from "primeng/button";
import {delay} from "rxjs";
import {LoadingPageComponent} from "../../shared/loading-page/loading-page.component";
import { AvatarModule } from 'primeng/avatar';

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

  public chatMessages: {
    user: string,
    waiting: boolean
    message?: string
  }[] = [{user: 'You', waiting: false, message: 'Switch on the lights please'}, {
    user: 'Zühlki assistant',
    waiting: true,
    message: 'Certainly my friend. There will be light!'
  }];

  async ngOnInit(): Promise<void> {
    setTimeout(() => {
      this.chatMessages[this.chatMessages.length - 1].waiting = false;
    }, 3000);
  }
}

