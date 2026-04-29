export interface Character {
  id: string;
  name: string;
  persona: string;
  speaking_style: string;
  avatar?: string;
}

export interface CharacterListResponse {
  characters: Character[];
}
