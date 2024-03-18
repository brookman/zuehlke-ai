import {Injectable} from '@angular/core';
import {WebSocketSubject} from "rxjs/internal/observable/dom/WebSocketSubject";

@Injectable({
  providedIn: 'root',
})
export class WebsocketService {
  private websocket?: WebSocketSubject<any>;

  constructor() {
  }

  connect() {
    const websocket = new WebSocketSubject('ws://localhost:8080/ws');

    websocket.subscribe(
      msg => console.log('message received: ', msg), // Called whenever there is a message from the server.
      err => console.log(err), // Called if at any point WebSocket API signals some kind of error.
      () => console.log('complete') // Called when connection is closed (for whatever reason).
    );

    this.websocket = websocket;
  }

  disconnect() {
    this.websocket?.complete();
    console.log('Disconnected');
  }

  sendMessage(message: string) {
    this.websocket?.next(message);
  }
}
