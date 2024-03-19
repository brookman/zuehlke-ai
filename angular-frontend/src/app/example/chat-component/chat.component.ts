import {Component, OnDestroy, OnInit} from '@angular/core';
import {FormControl, FormGroup, ReactiveFormsModule, Validators} from "@angular/forms";
import {InputTextareaModule} from "primeng/inputtextarea";
import {ButtonModule} from "primeng/button";
import {InputIconModule} from "primeng/inputicon";
import {IconFieldModule} from "primeng/iconfield";
import {InputGroupModule} from "primeng/inputgroup";
import {InputTextModule} from "primeng/inputtext";
import {WebsocketService} from "../../shared/websocket-service/websocket.service";
import {ChatService} from "../../shared/chat-service/chat.service";
import {ButtonGroupModule} from "primeng/buttongroup";
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {environment} from '../../../environments/environment';


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
    InputTextModule,
    ButtonGroupModule
  ],
  templateUrl: './chat.component.html',
  styleUrl: './chat.component.scss'
})
export class ChatComponent implements OnInit, OnDestroy {

  formGroup!: FormGroup;
  public isLoading = false;

  isRecording = false;
  mediaRecorder!: MediaRecorder;
  audioChunks: Blob[] = [];
  transcription = '';

  private hfApiEndpoint = environment.hfApiEndpoint;
  private hfApiToken = environment.hfApiToken;

  constructor(private chatService: ChatService, private http: HttpClient) {
  }

  ngOnInit() {
    this.formGroup = new FormGroup({
      prompt: new FormControl<string | null>({value: '', disabled: false}, Validators.required)
    });
  }

  handleSubmit() {
    const promptControl = this.formGroup.get('prompt');

    if (promptControl) {
      this.chatService.sendMessage(promptControl.value);
    }

    this.setInputValue("");
  }

  setInputValue(message: string) {
    const promptControl = this.formGroup.get('prompt');
    if (promptControl) {
      promptControl.setValue(message)
    }
  }

  async startRecording() {
    this.isRecording = true;
    this.audioChunks = [];
    this.setInputValue("");
    try {
      const stream = await navigator.mediaDevices.getUserMedia({audio: true});
      this.mediaRecorder = new MediaRecorder(stream);
      this.mediaRecorder.ondataavailable = (event) => {
        this.audioChunks.push(event.data);
      };
      this.mediaRecorder.onstop = async () => {
        const audioBlob = new Blob(this.audioChunks, {type: 'audio/flac'});
        // Convert Blob to File if necessary
        const audioFile = new File([audioBlob], "filename.flac", {type: 'audio/flac'});
        this.transcription = await this.sendAudioToAPI(audioFile);

        const promptControl = this.formGroup.get('prompt');
        this.setInputValue(this.transcription);
        console.log(this.transcription); // Log the transcription to verify it works
      };
      this.mediaRecorder.start();
    } catch (error) {
      console.error('Error accessing the microphone', error);
    }
  }

  stopRecording() {
    this.isRecording = false;
    this.mediaRecorder.stop();
  }

  async sendAudioToAPI(file: Blob): Promise<string> {
    try {
      const headers = new HttpHeaders({
        'Authorization': 'Bearer '.concat(this.hfApiToken),
        'Accept': 'application/json',
      });
      const response = await this.http.post(this.hfApiEndpoint, file, {headers, responseType: 'json'}).toPromise();

      // Assuming the API returns a JSON object with a "text" property
      if (response && typeof response === 'object' && 'text' in response) {
        return response['text'] as string;
      }
      throw new Error('Invalid response structure'); // Throw an error if response structure is not as expected
    } catch (error) {
      console.error('Error sending audio to API', error);
      // Decide on a fallback string. Here, I'm using an empty string, but you might prefer something else.
      return '';
    }
  }

  ngOnDestroy() {
  }
}
