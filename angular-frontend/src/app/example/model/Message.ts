export interface Message {
  id: number,
  user: string,
  type: MessageType,
  waiting: boolean,
  message?: string
  imageUrl?: string
}

export enum MessageType {
  RESPONSE,
  REQUEST
}
