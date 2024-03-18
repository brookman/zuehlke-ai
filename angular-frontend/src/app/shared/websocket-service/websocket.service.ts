import {Injectable} from "@angular/core";
import * as Rx from "rxjs";

@Injectable({
  providedIn: "root"
})
export class WebsocketService {
  constructor() {
  }

  private subject?: Rx.Subject<MessageEvent>;
  private websocket?: WebSocket;

  public connect(url: string): Rx.Subject<MessageEvent> {
    if (!this.subject) {
      this.subject = this.create(url);
      console.log("Successfully connected: " + url);
    }
    return this.subject;
  }

  public disconnect() {
    this.websocket?.close();
  }

  private create(url: string): Rx.Subject<MessageEvent> {
    let ws = new WebSocket(url);

    let observable = new Rx.Observable(subscriber => {
      ws.onmessage = subscriber.next.bind(subscriber);
      ws.onerror = subscriber.error.bind(subscriber);
      ws.onclose = subscriber.complete.bind(subscriber);
      return ws.close.bind(ws);
    });

    let observer = {
      next: (data: Object) => {
        if (ws.readyState === WebSocket.OPEN) {
          ws.send(JSON.stringify(data));
        }
      }
    };
    return Rx.Subject.create(observer, observable);
  }
}
