export interface PrometheusResponse {
  status: string;
  data: {
    resultType: string;
    result: PrometheusResult[];
  };
}

export interface PrometheusResult {
  metric: {
    __name__: string;
    instance: string;
    job: string;
  };
  value: [number, string];
}
