import {Component, OnDestroy, OnInit} from '@angular/core';
import {RemoteService} from '../../shared/remote-service/remote.service';
import {ExampleDto} from '../model/example-dto.model';
import {MotdDto} from '../model/MotdDto';
import {firstValueFrom, forkJoin} from 'rxjs';
import {WebsocketService} from '../../shared/websocket-service/websocket.service';

@Component({
  selector: 'example-component',
  templateUrl: './example.component.html',
  styleUrls: ['./example.component.scss']
})
export class ExampleComponent implements OnInit, OnDestroy {
  public exampleDto?: ExampleDto;
  public motdDto?: MotdDto;
  public isLoading = true;

  constructor(private remoteService: RemoteService, private websocketService: WebsocketService) {
  }

  public ngOnInit(): void {
    //this.loadExamples();
    this.connectWebsocket().then(value => {
      console.log('Connected');
    });
  }

  public async connectWebsocket(): Promise<void> {
    this.isLoading = true;

    this.websocketService.connect();

    this.websocketService.sendMessage('Hello from Angular');

    this.isLoading = false;
  }

  ngOnDestroy() {
    this.websocketService.disconnect();
  }

  public async loadExamples(): Promise<void> {
    this.isLoading = true;

    const data = await firstValueFrom(forkJoin({
      exampleDto: this.remoteService.get<ExampleDto>(""),
      // motdDto: this.remoteService.get<MotdDto>("motd")
    }));

    if (data.exampleDto) {
      this.exampleDto = data.exampleDto;
    }

/*    if (data.motdDto) {
      this.motdDto = data.motdDto;
    } */

    this.isLoading = false;
  }
}
