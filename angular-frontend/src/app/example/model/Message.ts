export interface Message {
  id: number,
  user: string,
  waiting: boolean,
  message?: string
}
