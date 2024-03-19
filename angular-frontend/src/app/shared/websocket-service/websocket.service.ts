import {Injectable} from '@angular/core';
import {WebSocketSubject} from "rxjs/internal/observable/dom/WebSocketSubject";
import {Observable, Subject} from "rxjs";

@Injectable({
  providedIn: 'root',
})
export class WebsocketService {
  private websocket?: WebSocketSubject<any>;
  constructor() {
  }

  connect(): WebSocketSubject<any>{
    if (!this.websocket || this.websocket.closed) {
      this.websocket = new WebSocketSubject('ws://localhost:8080/ws');
    }
    return this.websocket;
  }

  disconnect() {
    if (this.websocket && !this.websocket.closed) {
      this.websocket.complete();
      console.log('Disconnected');
    }
/*    this.websocket?.complete();
    console.log('Disconnected');*/
  }

  sendMessage(message: string) {
    this.websocket?.next(message);
  }
}
