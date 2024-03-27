import {Component, OnDestroy, OnInit} from '@angular/core';
import {FormControl, FormGroup, ReactiveFormsModule} from "@angular/forms";
import {InputTextareaModule} from "primeng/inputtextarea";
import {ButtonModule} from "primeng/button";
import {InputIconModule} from "primeng/inputicon";
import {IconFieldModule} from "primeng/iconfield";
import {InputGroupModule} from "primeng/inputgroup";
import {InputTextModule} from "primeng/inputtext";
import {ChatService} from "../../shared/chat-service/chat.service";
import {ButtonGroupModule} from "primeng/buttongroup";
import {HttpClient} from "@angular/common/http";
import {environment} from '../../../environments/environment';
import {ActionComponent} from "../action/action.component";
import {AnimateOnScrollModule} from "primeng/animateonscroll";
import {OpenAI} from 'openai';


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

  private openai = new OpenAI({
    organization: environment.openAiOrganization,
    apiKey: environment.openAiApiToken,
    dangerouslyAllowBrowser: true,
  });

  constructor(private chatService: ChatService, private http: HttpClient) {
  }

  ngOnInit() {
    this.formGroup = new FormGroup({
      prompt: new FormControl<string | null>({value: '', disabled: false})
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
    const audioType = 'audio/webm';
    const fileName = 'audio.webm';

    this.isRecording = true;
    this.audioChunks = [];
    this.setInputValue("");
    try {
      const stream = await navigator.mediaDevices.getUserMedia({audio: true});
      this.mediaRecorder = new MediaRecorder(stream, {mimeType: audioType});
      this.mediaRecorder.ondataavailable = (event) => {
        this.audioChunks.push(event.data);
      };
      this.mediaRecorder.onstop = async () => {
        const audioBlob = new Blob(this.audioChunks, {type: audioType});
        const audioFile = new File([audioBlob], fileName, {type: audioType});

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

  async sendAudioToAPI(audioFile: File): Promise<string> {
    try {

      const response = await this.openai.audio.transcriptions.create({
        file: audioFile,
        model: 'whisper-1'
      });

      return response.text;
    } catch (error) {
      console.error('Error sending audio to API', error);
      // Decide on a fallback string. Here, I'm using an empty string, but you might prefer something else.
      return '';
    }
  }

  async textToSpeech(inputText: string): Promise<void> {
    try {

      const response = await this.openai.audio.speech.create({
        input: inputText,
        model: 'tts-1',
        voice: 'alloy',
        response_format: 'mp3',
      });

      const blob = await response.blob();
      const blobUrl = URL.createObjectURL(blob);
      const audio = new Audio(blobUrl);

      audio.load();
      await audio.play();

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
