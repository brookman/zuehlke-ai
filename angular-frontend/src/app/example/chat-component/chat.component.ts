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
import {ActionComponent} from "../action/action.component";
import {AnimateOnScrollModule} from "primeng/animateonscroll";


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
    ButtonGroupModule,
    ActionComponent,
    AnimateOnScrollModule
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
  private openAiApiEndpoint = environment.openAiApiEndpoint;
  private openAiApiToken = environment.openAiApiToken;

  constructor(private chatService: ChatService, private http: HttpClient) {
  }

  ngOnInit() {
    this.formGroup = new FormGroup({
      prompt: new FormControl<string | null>({value: '', disabled: false })
    });

    // Subscribe to onNewMessage events
    this.chatService.onNewMessage.subscribe((message: string) => {
      this.textToSpeech(message);
    });
  }

  handleSubmit() {
    const promptControl = this.formGroup.get('prompt');

    if (promptControl) {
      this.chatService.sendMessage(promptControl.value);
    }

    this.formGroup.reset();
    // this.setInputValue("");
  }

  setInputValue(message: string) {
    console.log("setInputValue called")
    const promptControl = this.formGroup.get('prompt');
    if (promptControl) {
      promptControl.setValue(message);
    }
    this.formGroup.updateValueAndValidity();
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
        console.log(this.transcription); // Log the transcription to verify it works
        this.setInputValue(this.transcription);
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

  async textToSpeech(inputText: string): Promise<void> {
    try {
      const headers = new HttpHeaders({
        'Authorization': 'Bearer '.concat(this.openAiApiToken),
        'Content-Type': 'application/json',
        'Accept': 'audio/mpeg'
      });

      const body = JSON.stringify({
        model: "tts-1",
        voice: "alloy",
        input: inputText
      });

      // Note: Adjust the API endpoint as necessary
      const response = await this.http.post(this.openAiApiEndpoint, body, { headers, responseType: 'blob' }).toPromise();

      // Assuming the API returns the speech audio as a blob, play the audio
      const url = URL.createObjectURL(response as Blob);
      let audio = new Audio(url);
      audio.load();
      audio.play();
    } catch (error) {
      console.error('Error with text-to-speech API', error);
    }
  }

  ngOnDestroy() {
    this.chatService.onNewMessage.unsubscribe();
  }

  handleAction(message: string) {
    this.chatService.sendMessage(message);
  }
}
