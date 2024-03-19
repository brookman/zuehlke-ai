import {Component, OnDestroy, OnInit} from '@angular/core';
import {FormControl, FormGroup, ReactiveFormsModule, Validators} from "@angular/forms";
import {InputTextareaModule} from "primeng/inputtextarea";
import {ButtonModule} from "primeng/button";
import {InputIconModule} from "primeng/inputicon";
import {IconFieldModule} from "primeng/iconfield";
import {InputGroupModule} from "primeng/inputgroup";
import {InputTextModule} from "primeng/inputtext";
import {WebsocketService} from "../../shared/websocket-service/websocket.service";

@Component({
  selector: 'chat-component',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    InputTextareaModule,
    ButtonModule,
    InputIconModule,
    IconFieldModule,
    InputGroupModule,
    InputTextModule
  ],
  templateUrl: './chat.component.html',
  styleUrl: './chat.component.scss'
})
export class ChatComponent implements OnInit, OnDestroy{

  formGroup!: FormGroup;
  public isLoading = false;

  constructor(private websocketService: WebsocketService) {
  }

  ngOnInit() {
    this.formGroup = new FormGroup({
      prompt: new FormControl<string | null>({value: '', disabled: false}, Validators.required)
    });
  }

  handleSubmit() {
    const promptControl = this.formGroup.get('prompt');

    if (promptControl) {
      this.websocketService.sendMessage(promptControl.value);
    }

  }

  ngOnDestroy() {
    this.websocketService.disconnect();
  }
}
