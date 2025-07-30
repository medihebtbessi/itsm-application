export enum Role {
  MANAGER = 'MANAGER',
  ADMIN = 'ADMIN',
  ENGINEER = 'ENGINEER',
  USER = 'USER',
}
export enum Group {
  SOFTWARE = 'SOFTWARE',
  NETWORK = 'NETWORK',
  HARDWARE = 'HARDWARE',
}

export interface RegistrationRequest {
  firstname: string;
  lastname: string;
  email: string;
  password: string;
  role: Role;
  group: Group
}
